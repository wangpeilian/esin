package com.esin.jdbc.service;

import com.esin.jdbc.define.Transactional;

public interface ICommonService {

    @Transactional
    public void doTransactionTask(Runnable task);

}
