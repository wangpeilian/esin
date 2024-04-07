package com.esin.jdbc.service;

import com.esin.jdbc.define.Transactional;

public class CommonService implements ICommonService {

    @Override
    @Transactional
    public void doTransactionTask(Runnable task) {
        task.run();
    }
}
