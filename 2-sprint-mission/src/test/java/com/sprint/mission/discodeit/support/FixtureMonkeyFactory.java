package com.sprint.mission.discodeit.support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

public final class FixtureMonkeyFactory {

  private static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .build();

  private FixtureMonkeyFactory() {
  }

  public static FixtureMonkey get() {
    return INSTANCE;
  }
}