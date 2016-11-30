package com.yuralex.poketool;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyCharacterMap;

import com.pokegoapi.api.pokemon.Pokemon;

public class PokemonDto implements Parcelable {
    private int pokemonId;
    private String pokemonName;
    private int cp;
    private long creationTimeMs;
    private String nickname;
    private float level;
    private byte individualAttack;
    private byte individualDefense;
    private byte individualStamina;

    public PokemonDto(Pokemon p) {
        pokemonId = p.getPokemonId().getNumber();
        pokemonName = p.getPokemonId().name();
        cp = p.getCp();
        creationTimeMs = p.getCreationTimeMs();
        nickname = p.getNickname();
        level = p.getLevel();
        individualAttack = (byte)p.getIndividualAttack();
        individualDefense = (byte)p.getIndividualDefense();
        individualStamina = (byte)p.getIndividualStamina();
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PokemonDto> CREATOR = new Parcelable.Creator<PokemonDto>() {
        public PokemonDto createFromParcel(Parcel in) {
            return new PokemonDto(in);
        }

        public PokemonDto[] newArray(int size) {
            return new PokemonDto[size];
        }
    };

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(pokemonId);
        out.writeString(pokemonName);
        out.writeInt(cp);
        out.writeLong(creationTimeMs);
        out.writeString(nickname);
        out.writeFloat(level);
        out.writeByte(individualAttack);
        out.writeByte(individualDefense);
        out.writeByte(individualStamina);
    }

    /** recreate object from parcel */
    private PokemonDto(Parcel in) {
        pokemonId = in.readInt();
        pokemonName = in.readString();
        cp = in.readInt();
        creationTimeMs = in.readLong();
        nickname = in.readString();
        level = in.readFloat();
        individualAttack = in.readByte();
        individualDefense = in.readByte();
        individualStamina = in.readByte();
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public int getCp() {
        return cp;
    }

    public long getCreationTimeMs() {
        return creationTimeMs;
    }

    public String getNickname() {
        return nickname;
    }

    public float getIvRatio() {
        return (individualAttack + individualDefense + individualStamina) / 45.0f;
    }

    public float getLevel() {
        return level;
    }

    public byte getIndividualAttack() {
        return individualAttack;
    }

    public byte getIndividualDefense() {
        return individualDefense;
    }

    public byte getIndividualStamina() {
        return individualStamina;
    }
}
