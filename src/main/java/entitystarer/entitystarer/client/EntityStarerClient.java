package entitystarer.entitystarer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class EntityStarerClient implements ClientModInitializer {

    // keybind which to toggle staring at an entity
    private static KeyBinding stareKeyBind;
    // whether is toggled or not
    private static boolean toggle;
    // entity to stare at
    private static Entity lookingAt;

    @Override
    public void onInitializeClient() {

        lookingAt = null;
        toggle = false;

        // default is R. probably a mistake.
        stareKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.entitystarer.stare",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.entitystarer,main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // set toggle if needed
            if (stareKeyBind.wasPressed()) {toggle = !toggle;}
            // make sure player exists
            if (client.player == null) return;
            // if is toggled off, set our looking at to be none
            if (!toggle) {
                lookingAt = null;
                return;
            }
            // else is toggled on
            // if no entity found
            if (lookingAt == null) {
                // tell player
                client.player.sendMessage(Text.of("Looking for entity..."), true);

                // get entity
                HitResult hit = client.crosshairTarget;
                if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;
                lookingAt = ((EntityHitResult) hit).getEntity(); // set entity
            }

            // make sure entity is alive
            // if is dead then stop staring. it's impolite
            if (!lookingAt.isAlive()) {
                lookingAt = null;
                toggle = false;
                return;
            }

            // otherwise, stare away my friend.
            client.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, lookingAt.getEyePos());
            client.player.sendMessage(Text.of("Staring..."), true);

        });
    }
}
