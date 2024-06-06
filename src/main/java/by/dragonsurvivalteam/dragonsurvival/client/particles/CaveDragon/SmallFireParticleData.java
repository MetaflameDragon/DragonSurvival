package by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon;

import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class SmallFireParticleData implements ParticleOptions{
	public static final Deserializer<SmallFireParticleData> DESERIALIZER = new Deserializer<SmallFireParticleData>(){
		@Override
		public SmallFireParticleData fromCommand(ParticleType<SmallFireParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException{
			reader.expect(' ');
			float duration = (float)reader.readDouble();
			reader.expect(' ');
			boolean swirls = reader.readBoolean();
			return new SmallFireParticleData(duration, swirls);
		}

		@Override
		public SmallFireParticleData fromNetwork(ParticleType<SmallFireParticleData> particleTypeIn, FriendlyByteBuf buffer){
			return new SmallFireParticleData(buffer.readFloat(), buffer.readBoolean());
		}
	};

	private final float duration;
	private final boolean swirls;

	public static Codec<SmallFireParticleData> CODEC(ParticleType<SmallFireParticleData> particleType){
		return RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(Codec.FLOAT.fieldOf("duration").forGetter(SmallFireParticleData::getDuration), Codec.BOOL.fieldOf("swirls").forGetter(SmallFireParticleData::getSwirls)).apply(codecBuilder, SmallFireParticleData::new));
	}

	public SmallFireParticleData(float duration, boolean spins){
		this.duration = duration;
		swirls = spins;
	}

	@OnlyIn( Dist.CLIENT )
	public float getDuration(){
		return duration;
	}

	@OnlyIn( Dist.CLIENT )
	public boolean getSwirls(){
		return swirls;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer){
		buffer.writeFloat(duration);
		buffer.writeBoolean(swirls);
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public String writeToString(){
		return String.format(Locale.ROOT, "%s %.2f %b", ForgeRegistries.PARTICLE_TYPES.getKey(getType()), duration, swirls);
	}

	@Override
	public ParticleType<SmallFireParticleData> getType(){
		return DSParticles.FIRE.get();
	}
}