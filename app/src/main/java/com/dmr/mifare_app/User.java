package com.dmr.mifare_app;

public class User {


    private String correo;
    private TagUser tag;

    public User(String correo, TagUser tag){
        this.correo = correo;
        this.tag = tag;

    }

    public User(String correo){
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public TagUser getTag() {
        return tag;
    }

    public void setTag(TagUser tag) {
        this.tag = tag;
    }

}



