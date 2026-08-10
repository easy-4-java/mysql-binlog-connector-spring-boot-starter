package com.alibaba.otter.canal.context;

import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * Thread-local Canal context that exposes the current {@link CanalModel} to event listeners,
 * using a transmittable thread local so that the context propagates across thread pools.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class CanalContext {

    private static TransmittableThreadLocal<CanalModel> threadLocal = new TransmittableThreadLocal<>();

    /**
     * Returns the {@link CanalModel} bound to the current thread.
     *
     * @return the current Canal model, or {@code null} if none is bound
     */
    public static CanalModel getModel(){
        return threadLocal.get();
    }


    /**
     * Binds the given {@link CanalModel} to the current thread.
     *
     * @param canalModel the Canal model to bind
     */
    public static void setModel(CanalModel canalModel){
        threadLocal.set(canalModel);
    }


    /**
     * Removes the {@link CanalModel} bound to the current thread.
     */
    public  static void removeModel(){
        threadLocal.remove();
    }
}
