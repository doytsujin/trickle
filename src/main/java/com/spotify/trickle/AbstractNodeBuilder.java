package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * TODO: document!
 */
abstract class AbstractNodeBuilder<N, R> implements ConnectedNodeBuilder<N> {
  protected final TrickleGraphBuilder<R> graphBuilder;
  protected final Node<N> node;
  protected final List<Value<?>> inputs;
  protected final List<Node<?>> predecessors;
  protected Function<Throwable, N> fallback = null;
  protected String nodeName = "unnamed";

  AbstractNodeBuilder(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
    this.graphBuilder = checkNotNull(graphBuilder, "graphBuilder");
    this.node = checkNotNull(node, "node");
    inputs = new ArrayList<>();
    predecessors = new ArrayList<>();
  }

  @Override
  public final ConnectedNode<N> connect() {
    // the argument count should be enforced by the API
    checkState(inputs.size() == argumentCount(), "PROGRAMMER ERROR: Incorrect argument count for node '%s' - expected %d, got %d", toString(), argumentCount(), inputs.size());

    return new ConnectedNode<>(nodeName, node, asDeps(inputs), predecessors, Optional.fromNullable(fallback));
  }

  int argumentCount() {
    return 0;
  }

  private static List<Dep<?>> asDeps(List<Value<?>> inputs) {
    List<Dep<?>> result = Lists.newArrayList();

    for (Object input : inputs) {
      if (input instanceof Name) {
        result.add(new BindingDep<>((Name<?>) input));
      } else if (input instanceof Node) {
        result.add(new NodeDep<>((Node<?>) input));
      } else {
        throw new IllegalStateException("PROGRAMMER ERROR: illegal input object: " + input);
      }
    }

    return result;
  }

  @Override
  public final Node<N> getNode() {
    return node;
  }

  @Override
  public String toString() {
    return nodeName;
  }

  @Override
  public final Iterable<Value<?>> getInputs() {
    return inputs;
  }

  @Override
  public final Iterable<Node<?>> getPredecessors() {
    return predecessors;
  }

  protected void setFallback(Function<Throwable, N> handler) {
    fallback = checkNotNull(handler, "handler");
  }

  protected void setName(String name) {
    nodeName = checkNotNull(name, "name");
  }
}
