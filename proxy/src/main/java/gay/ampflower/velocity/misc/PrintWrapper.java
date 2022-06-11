/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gay.ampflower.velocity.misc;// Created 2022-19-07T06:04:19

import java.io.OutputStream;
import java.io.PrintStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * {@link System#out} and {@link System#err} redirector.
 *
 * @author Ampflower
 * @since ${version}
 **/
public class PrintWrapper extends PrintStream {
  private final Logger logger;
  private final Level level;

  private int index;
  private final byte[] buf = new byte[8192];

  /**
   * Constructs a PrintStream to logger redirector.
   *
   * @param logger The logger to redirect to.
   * @param level The level to log at.
   * */
  public PrintWrapper(Logger logger, Level level) {
    super(OutputStream.nullOutputStream());
    this.logger = logger;
    this.level = level;
  }

  @Override
  public void write(int b) {
    if (b == '\n' || (b & ~255) != 0) {
      logger.atLevel(level).log(new String(buf, 0, index));
      index = 0;
    } else {
      buf[index++] = (byte) b;
      if (index == 8192) {
        forcePrint();
      }
    }
  }

  @Override
  public void write(byte @NotNull [] buf, int off, int len) {
    while (index + len > 8192) {
      int len0 = 8192 - index;
      System.arraycopy(buf, off, this.buf, index, len0);
      len -= len0;
      printLines(8192);
      if (index == 8192) {
        forcePrint();
      }
    }
    System.arraycopy(buf, off, this.buf, index, len);
    printLines(index + len);
  }

  @Override
  public void flush() {
    forcePrint();
  }

  private void forcePrint() {
    logger.atLevel(level).log(new String(buf, 0, index));
    index = 0;
  }

  private void printLines(int l) {
    int i = 0;
    int p = 0;
    for (; i < l; i++) {
      if (buf[i] == '\n') {
        logger.atLevel(level).log(new String(buf, p, i));
        p = ++i;
      }
    }
    System.arraycopy(buf, p, buf, 0, index = l - p);
  }
}
