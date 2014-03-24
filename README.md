# graf-znak

A Clojure library designed to run arbitrary accumulations across groups. It is written using [core.typed](https://github.com/clojure/core.typed).

## Usage

Graf-znak is composed of a few main abstractions:

* Accumulator - a wrapper around a function that will, well, accumulate for you
* Hook - a set of attribute names and a set of accumulators that will run on groups in that attribute set
* HookStorage - a backing storage system for the state of hooks
* Net - a stateful collection of hooks and their backing storage

Say, for example, that we have a list of people, like

```clojure
({:name "Pat Shields" :age 28 :city "Durham"}
 {:name "John Smith" :age 34 :city "Raleigh"}
 ...)
```

Now, say we want to find the number of people of each age in each city. So we want to end up with something like

```clojure
{(28 "Durham") {:count 1} (34 "Raleigh") {:count 1}, ...}
```

We can do that by doing the following:
```clojure
(def hook (->Hook [:name :age] [accumulators/counter]))
(def net (create-net hook atom-storage/factory))
(doseq [person people] (send-net net person))
(check-net net hook) ; {(28 "Durham") {:count 1} ...)
```

### Accumulators

Graf-znak ships with a few built in accumulators, located in the ```graf-znak.accumulators``` namespace:

* counter - A pure accumulator that simply counts each instance passed to it.
* stateful-counter - An impure counting accumulator. Meant to be used with unwrapped mutable backing storage.
* unique (factory provided) - A pure accumulator that will capture all unique combinations of specific fields.

You can (and should) also build your own accumulators. There is a record type Accumulator that contains:

* name - a unique name for the accumulator (collisions can occur within a single hook)
* init-fn - a function that will be called with no arguments that should return an initial state for the accumulator
* fn - a function that will be called for every new value and will be passed the state and the full input record.

### Hooks

Hooks are just collections of accumulators tied to a specific collection of columns to group by. Hooks are implemented as a record that contains:

* fields - a list of fields that will be used for grouping
* accumulators - a list of accumulators that will be called on all values in this grouping.

Records that do not contain all the fields in the hook's fields list will be ignored by the hook.

### HookStorage

Hooks describe the mechanism for running accumulations, but the actual data they produce is stored in HookStorage. Graf-znak ships with two different types of HookStorage right now:

* atom-storage - Uses clojure STM to control access across threads. Accumulators should be pure functions and return the new state they want stored.
* concurrent-hash-storage - Uses a java ```ConcurrentHashMap``` under the covers. Accumulators should mutate the state they recieve.

In general, atom-storage should be used unless your use case has extreme performance requirements.

You can also build your own ```HookStorage``` by implementing the ```HookStorage``` protocol.

### Nets

Nets are collections of hooks and a backing storage system for those hooks. Use like so:

```
(use 'graf-znak.core)
(def my-net (create-net my-hooks my-storage-factory))
(put my-net {:a 1 :b 2 :c 3})
(check my-net my-hook) ; {(1 3) 1}
```

## Status

So very pre-alpha. You probably want ztellman's [narrator](https://github.com/ztellman/narrator) anyways.

Graf-znak is really just an idea I am playing with. Mostly I'm hoping someone will tell me I am crazy and that there is a way easier way to accomplish this.

## License

Copyright 2014 Patrick Shields

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
