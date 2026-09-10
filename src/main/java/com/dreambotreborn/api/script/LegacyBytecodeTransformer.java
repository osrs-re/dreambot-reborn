package com.dreambotreborn.api.script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rewrites references to the legacy DreamBot API in a class-file constant pool.
 *
 * <p>The original script JAR is never modified. Besides package names, this
 * transformer adapts compatible method descriptors. That second step matters
 * for already compiled scripts: {@code List all()} and {@code Query all()} are
 * source-compatible when Query implements List, but they are different JVM
 * method descriptors.</p>
 */
final class LegacyBytecodeTransformer
{
    static final String LEGACY_INTERNAL = "org/dreambot/api/";
    static final String CURRENT_INTERNAL = "com/dreambotreborn/api/";
    static final String LEGACY_BINARY = "org.dreambot.api.";
    static final String CURRENT_BINARY = "com.dreambotreborn.api.";
    private static final int CLASS_MAGIC = 0xCAFEBABE;

    private LegacyBytecodeTransformer() { }

    static Result transform(byte[] classFile, ClassLoader apiLoader) throws IOException
    {
        if (classFile == null || classFile.length < 10)
            throw new IOException("Invalid class file");

        int minor;
        int major;
        byte[] remainder;
        List<Constant> pool;
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classFile)))
        {
            int magic = input.readInt();
            if (magic != CLASS_MAGIC) throw new IOException("Invalid class-file magic");
            minor = input.readUnsignedShort();
            major = input.readUnsignedShort();
            int constantPoolSize = input.readUnsignedShort();
            pool = new ArrayList<>(constantPoolSize + 16);
            pool.add(null);
            while (pool.size() < constantPoolSize)
            {
                Constant constant = Constant.read(input);
                pool.add(constant);
                if (constant.tag == 5 || constant.tag == 6) pool.add(null);
            }
            remainder = new byte[input.available()];
            input.readFully(remainder);
        }

        boolean changed = false;
        int originalPoolSize = pool.size();
        for (int index = 1; index < originalPoolSize; index++)
        {
            Constant constant = pool.get(index);
            if (constant == null || constant.tag != 1) continue;
            String remapped = remap(constant.text);
            if (!constant.text.equals(remapped))
            {
                constant.text = remapped;
                changed = true;
            }
        }

        int adaptedMethodReferences = adaptMethodDescriptors(pool, originalPoolSize, apiLoader);
        changed |= adaptedMethodReferences > 0;
        if (!changed) return new Result(classFile, false, 0);
        if (pool.size() > 0xFFFF) throw new IOException("Class-file constant pool is too large");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(classFile.length + 256);
        try (DataOutputStream output = new DataOutputStream(bytes))
        {
            output.writeInt(CLASS_MAGIC);
            output.writeShort(minor);
            output.writeShort(major);
            output.writeShort(pool.size());
            for (int index = 1; index < pool.size(); index++)
            {
                Constant constant = pool.get(index);
                if (constant != null) constant.write(output);
            }
            output.write(remainder);
        }
        return new Result(bytes.toByteArray(), true, adaptedMethodReferences);
    }

    private static int adaptMethodDescriptors(List<Constant> pool, int limit,
                                               ClassLoader apiLoader)
    {
        if (apiLoader == null) return 0;
        int adapted = 0;
        for (int index = 1; index < limit; index++)
        {
            Constant reference = pool.get(index);
            if (reference == null || (reference.tag != 10 && reference.tag != 11)) continue;
            String owner = className(pool, reference.first);
            if (owner == null || !owner.startsWith(CURRENT_INTERNAL)) continue;
            Constant nameAndType = entry(pool, reference.second, 12);
            if (nameAndType == null) continue;
            String methodName = utf8(pool, nameAndType.first);
            String oldDescriptor = utf8(pool, nameAndType.second);
            if (methodName == null || oldDescriptor == null || methodName.startsWith("<")) continue;

            String replacement = compatibleDescriptor(owner, methodName, oldDescriptor, apiLoader);
            if (replacement == null || replacement.equals(oldDescriptor)) continue;

            int descriptorIndex = appendUtf8(pool, replacement);
            int nameAndTypeIndex = pool.size();
            pool.add(Constant.pair(12, nameAndType.first, descriptorIndex));
            reference.second = nameAndTypeIndex;
            adapted++;
        }
        return adapted;
    }

    private static String compatibleDescriptor(String owner, String name, String expectedDescriptor,
                                               ClassLoader apiLoader)
    {
        try
        {
            Class<?> ownerClass = Class.forName(owner.replace('/', '.'), false, apiLoader);
            MethodType expected = MethodType.parse(expectedDescriptor, apiLoader);
            return selectCompatibleDescriptor(ownerClass, name, expectedDescriptor, expected);
        }
        catch (ClassNotFoundException | IllegalArgumentException | LinkageError | SecurityException ignored)
        {
            return null;
        }
    }

    private static String selectCompatibleDescriptor(Class<?> ownerClass, String name,
                                                      String expectedDescriptor,
                                                      MethodType expected)
    {
        List<MethodCandidate> candidates = new ArrayList<>();
        for (Method method : ownerClass.getMethods())
        {
            if (!method.getName().equals(name) || !Modifier.isPublic(method.getModifiers())) continue;
            String descriptor = descriptor(method);
            if (descriptor.equals(expectedDescriptor)) return null;
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != expected.parameters.length) continue;

            int score = method.getReturnType().equals(expected.returnType) ? 0 : 1;
            if (!returnCompatible(expected.returnType, method.getReturnType())) continue;
            boolean compatible = true;
            for (int parameter = 0; parameter < parameters.length; parameter++)
            {
                Class<?> supplied = expected.parameters[parameter];
                Class<?> accepted = parameters[parameter];
                if (!parameterCompatible(supplied, accepted))
                {
                    compatible = false;
                    break;
                }
                if (!supplied.equals(accepted)) score++;
            }
            if (compatible) candidates.add(new MethodCandidate(descriptor, score));
        }
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingInt(candidate -> candidate.score));
        if (candidates.size() > 1 && candidates.get(0).score == candidates.get(1).score
            && !candidates.get(0).descriptor.equals(candidates.get(1).descriptor))
            return null;
        return candidates.get(0).descriptor;
    }

    private static boolean parameterCompatible(Class<?> supplied, Class<?> accepted)
    {
        if (supplied.isPrimitive() || accepted.isPrimitive()) return supplied.equals(accepted);
        return accepted.isAssignableFrom(supplied);
    }

    private static boolean returnCompatible(Class<?> expected, Class<?> actual)
    {
        if (expected == void.class || actual == void.class) return expected == actual;
        if (expected.isPrimitive() || actual.isPrimitive()) return expected.equals(actual);
        return expected.isAssignableFrom(actual);
    }

    private static String descriptor(Method method)
    {
        StringBuilder result = new StringBuilder("(");
        for (Class<?> parameter : method.getParameterTypes()) result.append(descriptor(parameter));
        return result.append(')').append(descriptor(method.getReturnType())).toString();
    }

    private static String descriptor(Class<?> type)
    {
        if (type.isArray()) return type.getName().replace('.', '/');
        if (!type.isPrimitive()) return "L" + type.getName().replace('.', '/') + ";";
        if (type == void.class) return "V";
        if (type == boolean.class) return "Z";
        if (type == byte.class) return "B";
        if (type == char.class) return "C";
        if (type == short.class) return "S";
        if (type == int.class) return "I";
        if (type == long.class) return "J";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    private static int appendUtf8(List<Constant> pool, String value)
    {
        int index = pool.size();
        pool.add(Constant.utf8(value));
        return index;
    }

    private static String className(List<Constant> pool, int index)
    {
        Constant type = entry(pool, index, 7);
        return type == null ? null : utf8(pool, type.first);
    }

    private static String utf8(List<Constant> pool, int index)
    {
        Constant value = entry(pool, index, 1);
        return value == null ? null : value.text;
    }

    private static Constant entry(List<Constant> pool, int index, int tag)
    {
        if (index <= 0 || index >= pool.size()) return null;
        Constant value = pool.get(index);
        return value != null && value.tag == tag ? value : null;
    }

    private static String remap(String value)
    {
        return value.replace(LEGACY_INTERNAL, CURRENT_INTERNAL)
            .replace(LEGACY_BINARY, CURRENT_BINARY);
    }

    static final class Result
    {
        final byte[] bytecode;
        final boolean changed;
        final int adaptedMethodReferences;

        Result(byte[] bytecode, boolean changed, int adaptedMethodReferences)
        {
            this.bytecode = bytecode;
            this.changed = changed;
            this.adaptedMethodReferences = adaptedMethodReferences;
        }
    }

    private static final class MethodCandidate
    {
        private final String descriptor;
        private final int score;

        private MethodCandidate(String descriptor, int score)
        {
            this.descriptor = descriptor;
            this.score = score;
        }
    }

    private static final class MethodType
    {
        private final Class<?>[] parameters;
        private final Class<?> returnType;

        private MethodType(Class<?>[] parameters, Class<?> returnType)
        {
            this.parameters = parameters;
            this.returnType = returnType;
        }

        private static MethodType parse(String descriptor, ClassLoader loader)
            throws ClassNotFoundException
        {
            if (descriptor == null || descriptor.isEmpty() || descriptor.charAt(0) != '(')
                throw new IllegalArgumentException("Invalid method descriptor");
            List<Class<?>> parameters = new ArrayList<>();
            int[] cursor = {1};
            while (cursor[0] < descriptor.length() && descriptor.charAt(cursor[0]) != ')')
                parameters.add(parseType(descriptor, cursor, loader));
            if (cursor[0] >= descriptor.length() || descriptor.charAt(cursor[0]++) != ')')
                throw new IllegalArgumentException("Invalid method descriptor");
            Class<?> returnType = parseType(descriptor, cursor, loader);
            if (cursor[0] != descriptor.length())
                throw new IllegalArgumentException("Trailing method descriptor data");
            return new MethodType(parameters.toArray(new Class<?>[0]), returnType);
        }

        private static Class<?> parseType(String descriptor, int[] cursor, ClassLoader loader)
            throws ClassNotFoundException
        {
            int start = cursor[0];
            if (start >= descriptor.length()) throw new IllegalArgumentException("Missing type");
            char kind = descriptor.charAt(cursor[0]++);
            switch (kind)
            {
                case 'V': return void.class;
                case 'Z': return boolean.class;
                case 'B': return byte.class;
                case 'C': return char.class;
                case 'S': return short.class;
                case 'I': return int.class;
                case 'J': return long.class;
                case 'F': return float.class;
                case 'D': return double.class;
                case 'L':
                    int end = descriptor.indexOf(';', cursor[0]);
                    if (end < 0) throw new IllegalArgumentException("Unterminated object type");
                    String binary = descriptor.substring(cursor[0], end).replace('/', '.');
                    cursor[0] = end + 1;
                    return Class.forName(binary, false, loader);
                case '[':
                    while (cursor[0] < descriptor.length()
                        && descriptor.charAt(cursor[0]) == '[') cursor[0]++;
                    if (cursor[0] >= descriptor.length())
                        throw new IllegalArgumentException("Unterminated array type");
                    if (descriptor.charAt(cursor[0]) == 'L')
                    {
                        int objectEnd = descriptor.indexOf(';', cursor[0]);
                        if (objectEnd < 0)
                            throw new IllegalArgumentException("Unterminated array object type");
                        cursor[0] = objectEnd + 1;
                    }
                    else cursor[0]++;
                    return Class.forName(descriptor.substring(start, cursor[0]).replace('/', '.'),
                        false, loader);
                default: throw new IllegalArgumentException("Unknown descriptor type " + kind);
            }
        }
    }

    private static final class Constant
    {
        private final int tag;
        private String text;
        private int first;
        private int second;
        private int intValue;
        private long longValue;

        private Constant(int tag) { this.tag = tag; }

        private static Constant utf8(String value)
        {
            Constant result = new Constant(1);
            result.text = value;
            return result;
        }

        private static Constant pair(int tag, int first, int second)
        {
            Constant result = new Constant(tag);
            result.first = first;
            result.second = second;
            return result;
        }

        private static Constant read(DataInputStream input) throws IOException
        {
            int tag = input.readUnsignedByte();
            Constant result = new Constant(tag);
            switch (tag)
            {
                case 1: result.text = input.readUTF(); break;
                case 3:
                case 4: result.intValue = input.readInt(); break;
                case 5:
                case 6: result.longValue = input.readLong(); break;
                case 7:
                case 8:
                case 16:
                case 19:
                case 20: result.first = input.readUnsignedShort(); break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18:
                    result.first = input.readUnsignedShort();
                    result.second = input.readUnsignedShort();
                    break;
                case 15:
                    result.first = input.readUnsignedByte();
                    result.second = input.readUnsignedShort();
                    break;
                default: throw new IOException("Unsupported constant-pool tag " + tag);
            }
            return result;
        }

        private void write(DataOutputStream output) throws IOException
        {
            output.writeByte(tag);
            switch (tag)
            {
                case 1: output.writeUTF(text); break;
                case 3:
                case 4: output.writeInt(intValue); break;
                case 5:
                case 6: output.writeLong(longValue); break;
                case 7:
                case 8:
                case 16:
                case 19:
                case 20: output.writeShort(first); break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18:
                    output.writeShort(first);
                    output.writeShort(second);
                    break;
                case 15:
                    output.writeByte(first);
                    output.writeShort(second);
                    break;
                default: throw new IOException("Unsupported constant-pool tag " + tag);
            }
        }
    }
}
