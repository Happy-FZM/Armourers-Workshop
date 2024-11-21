package moe.plushie.armourers_workshop.core.skin.animation.molang;

import moe.plushie.armourers_workshop.core.skin.animation.molang.ast.Constant;
import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.ObjectBinding;
import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.PrimaryBinding;
import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.holder.LambdaVariableHolder;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.runtime.Compiler;
import moe.plushie.armourers_workshop.core.skin.animation.molang.runtime.SyntaxException;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.init.ModLog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class MolangVirtualMachine {

    private static final Map<String, ObjectBinding> REGISTERED_BINDINGS = new ConcurrentHashMap<>();

    protected final Compiler compiler;

    public MolangVirtualMachine() {
        this(Collections.emptyMap());
    }

    public MolangVirtualMachine(Map<String, ObjectBinding> bindings) {
        var mergedBindings = new HashMap<>(REGISTERED_BINDINGS);
        mergedBindings.putAll(bindings);
        this.compiler = new Compiler(new PrimaryBinding(mergedBindings));
    }

    /**
     * Register a default object binding into virtual machine.
     *
     * @param name    the binding name.
     * @param binding the binding object.
     */
    public static void register(String name, ObjectBinding binding) {
        REGISTERED_BINDINGS.put(name, binding);
    }

    /**
     * Create a molang expression
     */
    public Expression compile(String source) throws SyntaxException {
        var expr = compiler.compile(source);
        if (ModConfig.Client.enableMolangDebug && !(expr instanceof Constant)) {
            ModLog.debug("source: {}", source);
            ModLog.debug("optimize: {}", expr);
        }
        return expr;
    }


    public void beginVariableCaching() {
        LambdaVariableHolder.push();
    }

    public void endVariableCaching() {
        LambdaVariableHolder.pop();
    }
}
