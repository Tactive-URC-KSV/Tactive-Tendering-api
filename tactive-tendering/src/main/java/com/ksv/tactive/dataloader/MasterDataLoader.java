package com.ksv.tactive.dataloader;

import java.io.IOException;
import org.springframework.dao.DataAccessException;

public interface MasterDataLoader<T> {
    void loadData() throws IOException, DataAccessException;
}