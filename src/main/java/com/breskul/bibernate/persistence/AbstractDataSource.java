package com.breskul.bibernate.persistence;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class AbstractDataSource implements DataSource {

  /**
   * Returns 0, indicating the default system timeout is to be used.
   */
  @Override
  public int getLoginTimeout() {
    return 0;
  }

  /**
   * Setting a login timeout is not supported.
   */
  @Override
  public void setLoginTimeout(int timeout) {
    throw new UnsupportedOperationException("setLoginTimeout");
  }

  /**
   * LogWriter methods are not supported.
   */
  @Override
  public PrintWriter getLogWriter() {
    throw new UnsupportedOperationException("getLogWriter");
  }

  /**
   * LogWriter methods are not supported.
   */
  @Override
  public void setLogWriter(PrintWriter pw) {
    throw new UnsupportedOperationException("setLogWriter");
  }

  /**
   * getParentLogger methods are not supported.
   */
  @Override
  public Logger getParentLogger() {
    throw new UnsupportedOperationException("getParentLogger");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }
    throw new SQLException("DataSource of type [" + getClass().getName() +
        "] cannot be unwrapped as [" + iface.getName() + "]");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return iface.isInstance(this);
  }
}
