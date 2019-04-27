package com.xuecheng.bean.utils;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class BeanCopyUtils {


    public static void copyProperties(Object source, Object target){
        copyProperties(source,target,null,null);
    }

    public static void copyProperties(Object source, Object target,String... ignoreProperties){
        copyProperties(source,target,null,ignoreProperties);
    }



    private static void copyProperties(Object source, Object target, @Nullable Class<?> editable,
                                       @Nullable String... ignoreProperties) throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        /**回去对象的class对象*/
        Class<?> actualEditable = target.getClass();
        /**
         *         // 若editable不为null，检查target对象是否是editable类的实例，若不是则抛出运行时异常
         *         // 这里的editable类是为了做属性拷贝时限制用的
         *         // 若actualEditable和editable相同，则拷贝actualEditable的所有属性
         *         // 若actualEditable是editable的子类，则只拷贝editable类中的属性
         */
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
                        "] not assignable to Editable class [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }

        /**
         *     PropertyDescriptor属性描述器
         * 　　1. getPropertyType()，获得属性的Class对象；
         * 　　2. getReadMethod()，获得用于读取属性值的方法；
         * 　　3. getWriteMethod()，获得用于写入属性值的方法；
         * 　　4. hashCode()，获取对象的哈希值；
         * 　　5. setReadMethod(Method readMethod)，设置用于读取属性值的方法；
         * 　　6. setWriteMethod(Method writeMethod)，设置用于写入属性值的方法。
         *
         */
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        /**
         * 封装那些需要的复制的字段名
         */
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

        for (PropertyDescriptor targetPd : targetPds) {

            /**
             * Method 提供关于类或接口上单独某个方法（以及如何访问该方法）的信息。所反映的方法可能是类方法或实例方法（包括抽象方法）
             * Method 允许在匹配要调用的实参与基础方法的形参时进行扩展转换；但如果要进行收缩转换，则会抛出 IllegalArgumentException。
             *
             * targetPd 获取值的对象(被复制的对象)
             * getWriteMethod() 获得用于写入属性值(set)的方法
             * */
            Method writeMethod = targetPd.getWriteMethod();
            /**
             *判断字段属性方法是否存在，判断是否有需要复制的字段
             */
            if (writeMethod != null && (ignoreList == null || ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    /**
                     *sourcePd 源对象(复制对象)
                     * getReadMethod() 获取用于读取(get)的方法
                     */
                    Method readMethod = sourcePd.getReadMethod();
                    /**
                     * ClassUtils.isAssignable(Class<?> cls, Class<?> toClass)
                     *                    该方法测试指定的类参数所表示的类型是否可以通过标识转换、展开原语转换或展开引用转换转换为该类对象所表示的类型
                     * writeMethod.getParameterTypes() 按照声明顺序返回 Type 对象的数组，这些对象描述了此 Method 对象所表示的方法的形参类型的
                     * readMethod.getReturnType() 返回一个 Class 对象，该对象描述了此 Method 对象所表示的方法的正式返回类型
                     *
                     * 表示入参是否可以转换为出参
                     */
                    if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            /**
                             * getDeclaringClass()  返回表示声明由此 Method 对象表示的方法的类或接口的 Class 对象。
                             * getModifiers() 以整数形式返回此 Method 对象所表示方法的 Java 语言修饰符。应该使用 Modifier 类对修饰符进行解码
                             * Modifier 对修饰符进行解码
                             */
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            /**
                             * invoke(Object obj,Object... args)
                             * obj - 从中调用基础方法的对象
                             * args - 用于方法调用的参数
                             * 对带有指定参数的指定对象调用由此 Method 对象表示的基础方法。个别参数被自动解包，以便与基本形参相匹配，基本参数和引用参数都随需服从方法调用转换。
                             * 如果基础方法是静态的，那么可以忽略指定的 obj 参数。该参数可以为 null。
                             * 如果基础方法所需的形参数为 0，则所提供的 args 数组长度可以为 0 或 null。
                             * 如果基础方法是实例方法，则使用动态方法查找来调用它，在发生基于目标对象的运行时类型的重写时更应该这样做。
                             * 如果基础方法是静态的，并且尚未初始化声明此方法的类，则会将其初始化。
                             * 如果方法正常完成，则将该方法返回的值返回给调用方；如果该值为基本类型，则首先适当地将其包装在对象中。
                             *      但是，如果该值的类型为一组基本类型，则数组元素不 被包装在对象中；换句话说，将返回基本类型的数组。如果基础方法返回类型为 void，则该调用返回 null。
                             */
                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }


    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
        /**
         * 内省(Introspector) 是对 JavaBean 类属性、事件的一种缺省处理方法。内省机制是通过反射来实现的
         * java Reflect 是在运行时状态将java类分别映射成不同java类（Field，Method，Contructor，Annoation,..）可以动态的获取所有的属性以及动态调用任意一个类行为即方法
         *
         * */
//        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return CachedIntrispectionResultsUtils.forClass(clazz).getPropertyDescriptors();
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName)
            throws BeansException {
        return CachedIntrispectionResultsUtils.forClass(clazz).getPropertyDescriptor(propertyName);
    }



}
