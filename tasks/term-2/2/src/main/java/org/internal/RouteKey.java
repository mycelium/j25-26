package org.internal;

import org.api.HttpMethod;

record RouteKey(HttpMethod method, String path) {}
