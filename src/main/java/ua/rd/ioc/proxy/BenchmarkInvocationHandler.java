package ua.rd.ioc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import ua.rd.ioc.annotation.Benchmark;

public class BenchmarkInvocationHandler implements InvocationHandler {

	private Object object;

	public BenchmarkInvocationHandler(Object object) {
		this.object = object;
	}
	

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method realMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());

		if (realMethod.getAnnotation(Benchmark.class) != null) {
			Long start = System.nanoTime();
			Object result = method.invoke(object, args);
			Long end = System.nanoTime();
			Long delta = end-start;
			System.out.println(delta.toString());
			return result;
		} else {
			return method.invoke(object, args);

		}

	}
}
