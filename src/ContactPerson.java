import java.util.Date;

public class ContactPerson {

    private int id;
    private String name;
    private String nickName;
    private String address;
    private String homePhone;
    private String workPhone;
    private String cellPhone;
    private String email;
    private Date birthDate;
    private String website;
    private String profession;

    public ContactPerson(int id, String name, String nickName, String address, String homePhone,
                         String workPhone, String cellPhone, String email, Date birthDate,
                         String website, String profession) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.address = address;
        this.homePhone = homePhone;
        this.workPhone = workPhone;
        this.cellPhone = cellPhone;
        this.email = email;
        this.birthDate = birthDate;
        this.website = website;
        this.profession = profession;
    }

    public ContactPerson() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHomePhone() { return homePhone; }
    public void setHomePhone(String homePhone) { this.homePhone = homePhone; }

    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }

    public String getCellPhone() { return cellPhone; }
    public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    @Override
    public String toString() {
        return "ContactPerson{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", address='" + address + '\'' +
                ", homePhone='" + homePhone + '\'' +
                ", workPhone='" + workPhone + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                ", website='" + website + '\'' +
                ", profession='" + profession + '\'' +
                '}';
    }
}