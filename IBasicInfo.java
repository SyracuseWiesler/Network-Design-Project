package corp;

interface IBasicInfo {
    public abstract void setAge(int age);
    public abstract void setSex(Sex sex);
    public abstract void setPosition(String position);
    public abstract void setAddress(int num, String street, String city, String province, int zipcode);
    public abstract void setTel(String tel);
    public abstract void setHostname(String hostname);
    public abstract void setHostip(String hostip);
    public abstract int getAge();
    public abstract Sex getSex();
    public abstract String getPosition();
    public abstract Address getAddress();
    public abstract String getTel();
    public abstract String getHostname();
    public abstract String getHostip();
}
