package com.yuralex.poketool;

public class PokemonImg {
    private int id;
    private int imageResourceId;

    public PokemonImg(int id, int imageResourceId){
        this.id = id;
        this.imageResourceId = imageResourceId;

    }

    public int getId() {
        return id;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}