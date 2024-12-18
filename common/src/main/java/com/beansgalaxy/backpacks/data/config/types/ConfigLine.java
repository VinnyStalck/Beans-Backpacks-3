package com.beansgalaxy.backpacks.data.config.types;

import com.google.gson.JsonObject;

public interface ConfigLine {
      String encode();

      String comment(int max);

      void decode(JsonObject jsonObject);

      default boolean punctuate() {
            return true;
      }
}
