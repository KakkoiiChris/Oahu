package kakkoiichris.oahu.runtime.linker;

import kakkoiichris.oahu.runtime.Memory;
import kakkoiichris.oahu.runtime.data.Instance;
import kakkoiichris.oahu.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LinkData(Instance instance, List<Object> args) {
    public <X> Optional<X> unlink(Class<X> clazz) {
        return Util.cast(clazz, instance.getLink());
    }

    public List<Object> unwrap() {
        var list = new ArrayList<>();

        for (var arg : args) {
            var value = Instance.fromInstance(Memory.fromReference(arg));

            list.add(value);
        }

        return list;
    }
}
