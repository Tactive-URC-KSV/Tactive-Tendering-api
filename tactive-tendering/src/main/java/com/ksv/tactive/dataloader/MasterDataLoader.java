package com.ksv.tactive.dataloader;
import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
public interface MasterDataLoader<T> extends Ordered {
    void loadData() throws IOException, DataAccessException;
}