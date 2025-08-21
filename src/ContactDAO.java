import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.mysql.cj.jdbc.MysqlDataSource;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.RowSetProvider;

//NOTES
/*
(وده مبدأ مهم في JDBC: لازم نقفل Connection, Statement, ResultSet بعد الاستخدام).

 */


public class ContactDAO {
    private Connection con;

    public ContactDAO() {
        connect();
        // awel m3ml new connectDAO hatsl bl DB
    }

    private void connect() {
        Properties props = new Properties();
        /*
         Properties: object bset key=value , 3shan store el properties zy ( DB_URL, USER, PASS)
         */
        FileInputStream fis = null; // dah 3shan a read mlf el properties
        try {
            fis = new FileInputStream("src/DP.properties"); // bfth el mlf 3shan a2rah
            props.load(fis); //يقرأ الملف ويملا props بالقيم.

            MysqlDataSource mysqlDS = new MysqlDataSource();
            /*
            هنا أنشأنا (Object) من نوع MysqlDataSource.

الـ MysqlDataSource ده كلاس موجود في مكتبة MySQL JDBC Driver.

وظيفته إنه يوفر وسيلة أسهل ومرنة لإدارة الاتصال بقاعدة البيانات بدل ما نكتب DriverManager.getConnection(...) كل مرة.

تقدر تعتبره زي "حامل إعدادات" connection.
             */
            mysqlDS.setURL(props.getProperty("DB_URL").replace("\"", ""));
            /*
            هنا بنحدد عنوان قاعدة البيانات (Database URL) اللي هيقول JDBC هيتصل فين.
props.getProperty() : BYGEB EL QEMA MN mlf el db.properties
             */
            mysqlDS.setUser(props.getProperty("USER").replace("\"", ""));
            /*
            هنا بنحدد اسم المستخدم (username) اللي هنستخدمه للاتصال بالـ Database.
             */
            mysqlDS.setPassword(props.getProperty("PASS").replace("\"", ""));

            con = mysqlDS.getConnection();//وهنا فعليًا بيتعمل اتصال بالـ Database باستخدام الإعدادات اللي خزناها.

            System.out.println("Database connected successfully via DataSource!");

        } catch (Exception e) {
            /*
            ده معناه: لو حصل أي خطأ أثناء الاتصال بالـ Database أو قراءة ملف الخصائص (DP.properties)، هيمسك الخطأ ويطبعه.
             */
            e.printStackTrace();//بيطبع تفاصيل الخطأ (اسم الاستثناء + الرسالة + مكان حدوثه) عشان نعرف إيه اللي حصل بالضبط.
        } finally {
            try {
                if (fis != null) fis.close();
                /*
                fis = كائن مسؤول عن قراءة ملف الخصائص (FileInputStream).

بعد ما خلصنا استخدامه، لازم نقفله (close) عشان نحرر الـ resource من الميموري والنظام.

if (fis != null) → بيتأكد إن الكائن اتعمل فعلًا ومش null.

fis.close() → بيقفل الملف.

لو حصل خطأ أثناء القفل (مثلا الملف اتقفل أصلا أو فيه مشكلة بالنظام)، بنمسكه بالـ catch الداخلي ونطبعه.
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConn() {
        /*
        دي دالة خاصة (private method) وظيفتها إغلاق الاتصال بالـ Database (Connection con) لما نخلص شغلنا.
         */
        try {
            if (con != null && !con.isClosed()) con.close();
            /*
            if (con != null && !con.isClosed())

بيتأكد الأول إن الاتصال (con) مش null (يعني تم إنشاؤه فعلًا).

وبعدين بيتأكد إنه مش مقفول بالفعل (!con.isClosed()).

ليه؟ عشان مايحاولش يقفل اتصال مش موجود أو مقفول → وده كان هيعمل Exception.

con.close();

يقفل الاتصال الفعلي مع الـ Database.

ده مهم جدًا عشان أي Connection مفتوح بياخد موارد من السيرفر ولو ما اتقفلش هيعمل leak (تسريب موارد).

catch (SQLException e)

لو حصل أي خطأ أثناء إغلاق الاتصال (زي إن السيرفر فصل فجأة أو الاتصال أصلاً مش صالح)، بيطبع الاستثناء.
             */
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        /*
        دي دالة عامة (public method) بتنده على closeConn().

الهدف منها: تعمل abstraction (تخفي التفاصيل).

بدل ما تنده على closeConn() (اللي هي private)، الكود الخارجي ينده ببساطة على close().
         */
        /*
        closeConn() = مسؤول عن قفل الـ Connection بأمان.

close() = واجهة عامة (public) يستعملها الكود الخارجي عشان يقفل الاتصال بسهولة.
         */
        closeConn();
    }

    // Insert new contact
    public boolean insertContactPerson(ContactPerson cp) {
        String sql = "INSERT INTO contact "
                + "(name, nick_name, address, home_phone, work_phone, cell_phone, email, birthday, web_site, profession) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        /*
        دي جملة SQL Insert علشان نضيف سجل جديد في جدول contact.

بنحدد الأعمدة اللي هنخزن فيها البيانات (name, nick_name, …).

بدل ما نكتب القيم جوة الجملة مباشرة، بنستخدم ? (placeholders).

ليه؟ عشان:

نمنع SQL Injection (أمان).

نقدر نمرر القيم بسهولة باستخدام PreparedStatement.
         */
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            //هنا بنستخدم try-with-resources → ده يضمن إن الـ PreparedStatement يتقفل أوتوماتيك حتى لو حصل Exception.
            //
            //con.prepareStatement(sql) بيجهز الجملة SQL علشان نقدر نملأ الـ ? بالقيم.
            ps.setString(1, cp.getName());
            ps.setString(2, cp.getNickName());
            ps.setString(3, cp.getAddress());
            ps.setString(4, cp.getHomePhone());
            ps.setString(5, cp.getWorkPhone());
            ps.setString(6, cp.getCellPhone());
            ps.setString(7, cp.getEmail());
            ps.setDate(8, cp.getBirthDate() == null ? null : new java.sql.Date(cp.getBirthDate().getTime()));
            ps.setString(9, cp.getWebsite());
            ps.setString(10, cp.getProfession());
            return ps.executeUpdate() > 0;
            /*
            ps.executeUpdate() بيرجع عدد الصفوف اللي اتأثرت (rows affected).

لو رجع رقم أكبر من 0 → يعني تم إدخال السجل بنجاح.

علشان كده بيرجع true لو العملية نجحت، وfalse لو فشلت.
             */
        } catch (SQLException e) {
            /*
            أي مشكلة تحصل أثناء التنفيذ (زي خطأ في SQL أو مشكلة اتصال) هتترمي كـ SQLException.

بنطبعها ونرجع false.
             */
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all contacts
    //الفكرة العامة
    //
    //الميثود getContacts() بتعمل:
    //
    //تجهّز List فاضية تستقبل النتائج.
    //
    //تبعت SQL: SELECT * FROM contact.
    //
    //تفتح Statement وتشغّل الكويري.
    //
    //تعدّي على الـ ResultSet صفّ صفّ، وتحوّل كل صفّ لأوبجكت ContactPerson (عن طريق createContactPerson(rs))، وتضيفه للّست.
    //
    //لو حصلت SQLException، بتطبع الـ stack trace.
    //
    //بترجّع الليست اللي فيها كل الكونتاكتس.
    public List<ContactPerson> getContacts() {
        List<ContactPerson> list = new ArrayList<>();  //بتجهّز ArrayList فاضية هنحط فيها النتائج اللي راجعة من الداتابيس.
        String sql = "SELECT * FROM contact"; //يعني هات كل الأعمدة من جدول contact
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { //try-with-resources
            //سلوب في جافا بيقفل الموارد أوتوماتيك بعد الانتهاء (حتى لو حصل Exception).
            //con.createStatement(): by3ml statement mortbt bl connection el mwgod f variable con (lazem ykon mtwsl bl DB)
            //st.executeQuery(sql):dah bysh8l el query w byrg3 result set dah zy pointer mashy 3la el rows el rg3t mn el DB
            //: بما إننا استخدمنا try-with-resources وفيه الاتنين (Statement و ResultSet) جوّا الأقواس، فالاتنين هيتقفلو تلقائيًا بترتيب عكسي بعد البلوك.
            while (rs.next()) {//بيحرّك المؤشّر على الصفّ التالي. أوّل مرّة بيودّينا على أوّل صفّ. بيرجّع true لو فيه صفّ، وfalse لما نخلص.
                list.add(createContactPerson(rs));//جوّا اللوب: createContactPerson(rs)  ميثود بتحوّل الصفّ الحالي لأوبجكت ContactPerson.
            }
        } catch (SQLException e) {
            //لو حصلت مشكلة JDBC/SQL (سيرفر وقع، كويري غلط، أسماء أعمدة غلط… إلخ)، هيمسكها الـ catch ويطبع التفاصيل.
            e.printStackTrace();
        }
        return list;//بترجّع الليست (لو حصل Exception، لسه هترجّع الليست—غالبًا فاضية).
    }


    // Retrieve contacts by name
    public List<ContactPerson> getContactsForName(String name) { //ميثود بترجع List<ContactPerson> لكل الأشخاص اللي اسمهم يحتوي على النصّ اللي انت مديّه.
        List<ContactPerson> list = new ArrayList<>(); //ليست فاضية هنحط فيها النتائج.
        String sql = "SELECT * FROM contact WHERE name LIKE ?";
        // ? hna placeholder (mkan qema hattht la7qn)
        //LIKE : m3naha dwer 3la el rows columns el qemetha feha pattern mo3yn
        // ex:
        //SELECT * FROM contact WHERE name LIKE '%ahmed%'
        //ده هيرجع أي contact اسمه فيه كلمة "ahmed".
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            //بنعمل PreparedStatement من الـ Connection.
            //
            //الفرق عن Statement: هنا نقدر نحط قيم آمنة مكان الـ ?.
            //
            //الفايدة:
            //
            //يمنع SQL Injection (لو اليوزر كتب اسم غريب).
            //
            //بيخلي الداتابيس يعمل كاش للكويري.
            ps.setString(1, "%" + name + "%");
            //بنحط القيمة الفعلية مكان أول ? في الكويري (عشان عندنا باراميتر واحد).
            //
            //"%" + name + "%" معناها أي حاجة قبل وبعد النصّ.
            //
            //مثال: لو name = "ali" → يتحول إلى '%ali%' → هيجيب كل اللي في اسمهم "ali".
            try (ResultSet rs = ps.executeQuery()) {
                //نشغّل الكويري ونستقبل النتائج في ResultSet.
                //
                //برضه مستخدمين try-with-resources، يعني الـ ResultSet هيتقفل تلقائيًا بعد البلوك.
                while (rs.next()) {
                    //نمشي على النتائج صفّ صفّ.
                    //
                    //كل صفّ بيتحوّل لأوبجكت ContactPerson عن طريق createContactPerson(rs).
                    //
                    //نضيفه في الليست.
                    list.add(createContactPerson(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); //لو حصل أي خطأ SQL، يطبع التفاصيل.
        }
        return list; //بيرجع الليست اللي فيها كل الكونتاكتس اللي اسمهم مطابق للـ pattern.
    }

    // Helper to create ContactPerson from ResultSet
    //دي هي الميثود المساعدة (helper)  واللي بتحوّل الصفّ (row) اللي جاي من الـ ResultSet إلى أوبجكت كامل من نوع ContactPerson
    private ContactPerson createContactPerson(ResultSet rs) throws SQLException {
        //private: الميثود دي داخلية (تستخدم جوه الكلاس بس).
        //
        //ContactPerson: نوع الـ return value (أوبجكت شخص).
        //
        //ResultSet rs: الصف الحالي اللي راجع من قاعدة البيانات.
        //
        //throws SQLException: معناها لو حصل خطأ في قراءة البيانات من الـ ResultSet، الميثود ممكن ترمي استثناء.
        ContactPerson cp = new ContactPerson(); //أنشأنا أوبجكت جديد فاضي من كلاس ContactPerson.
        cp.setId(rs.getInt("id")); //بيجيب العمود id (integer) من الـ ResultSet ويحطه في cp.id.
        cp.setName(rs.getString("name")); //بيقرأ العمود name كـ String ويحطه في cp.name.
        cp.setNickName(rs.getString("nick_name"));
        cp.setAddress(rs.getString("address"));
        cp.setHomePhone(rs.getString("home_phone"));
        cp.setWorkPhone(rs.getString("work_phone"));
        cp.setCellPhone(rs.getString("cell_phone"));
        cp.setEmail(rs.getString("email"));
        cp.setBirthDate(rs.getDate("birthday"));
        //نا خاص شوية: بيقرأ العمود birthday كـ java.sql.Date ويحطه في cp.birthDate.
        //
        //غالبًا الكلاس ContactPerson معرّف الـ birthDate كـ java.util.Date أو java.sql.Date.
        //
        // hna jdbc by3ml conversion lw field f db mn no3 DATE
        cp.setWebsite(rs.getString("web_site"));
        cp.setProfession(rs.getString("profession"));
        //نفس الفكرة: بياخد الأعمدة الأخيرة (web_site, profession) ويحطهم في الأوبجكت.
        return cp; //بعد ما ملينا كل fields، بيرجع الأوبجكت ContactPerson.
    }

    // Delete contact by ID
    //ميثود اسمها deleteContact، وظيفتها تحذف contact من الجدول contact باستخدام الـ id.
    //
    //بترجع boolean:
    //
    //true لو فعلاً اتحذف صفّ.
    //
    //false لو محصلش حذف (مثلاً: الـ id مش موجود، أو حصل Error).
    public boolean deleteContact(int id) {
        String sql = "DELETE FROM contact WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            //عملنا PreparedStatement من الاتصال con.
            //
            //ليه PreparedStatement؟ عشان:
            //
            //الأمان ضد SQL Injection.
            //
            //سهولة وضع الباراميتر بدل ما ن Concatenate String.
            //
            //إعادة استخدام الكويري (Performance أفضل).
            ps.setInt(1, id); //هنا بنقول: أول ? في الكويري، حط مكانه قيمة id اللي جاي من الباراميتر.
            return ps.executeUpdate() > 0;
            //executeUpdate() بترجع عدد الصفوف اللي اتأثرت بالكويري.
            //
            //لو id موجود → هيتمسح صف واحد على الأقل → القيمة ترجع 1 (أو أكتر لو الكويري بيأثر على أكتر من صف).
            //
            //لو id مش موجود → مفيش صفوف اتمسحت → القيمة ترجع 0.
            //
            //الشرط > 0 بيرجع:
            //
            //true = صف اتمسح.
            //
            //false = مفيش صفوف اتأثرت.
        } catch (SQLException e) { //لو حصلت مشكلة (زي: مشكلة اتصال بالداتابيس أو id غير صالح)، نطبع الـ stack trace ونرجّع false.
            e.printStackTrace();
            return false;
        }
    }

    // Update contact
    //الميثود دي مسؤولة عن تعديل بيانات contact موجود باستخدام الـ id

    public boolean updateContact(ContactPerson cp) {
        //ميثود بتاخد أوبجكت ContactPerson (فيه بيانات الشخص المطلوب تحديثه).
        //
        //بترجع boolean:
        //
        //true لو التحديث نجح (اتأثر على الأقل صفّ واحد).
        //
        //false لو مفيش صفوف اتعدّلت أو حصل خطأ
        String sql = "UPDATE contact SET name=?, nick_name=?, address=?, home_phone=?, work_phone=?, cell_phone=?, email=?, birthday=?, web_site=?, profession=? WHERE id=?";
        //SQL statement لتحديث بيانات الجدول.
        //
        //SET ... بيحدّد الأعمدة اللي هتتغير.
        //
        //WHERE id=? مهم جدًا → بيضمن إننا بنحدث صف واحد بس (اللي عنده الـ id المعطى).
        //لو نسيت WHERE id=? هتعمل Update لكل الجدول
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            //إنشاء PreparedStatement من الاتصال.
            //
            //نفس الفكرة اللي شرحناها: بيوفر أمان ضد SQL Injection وسهل نحط الباراميترز.

            //ربط الأعمدة بالبيانات من الـ ContactPerson
            ps.setString(1, cp.getName());
            ps.setString(2, cp.getNickName());
            ps.setString(3, cp.getAddress());
            ps.setString(4, cp.getHomePhone());
            ps.setString(5, cp.getWorkPhone());
            ps.setString(6, cp.getCellPhone());
            ps.setString(7, cp.getEmail());
            ps.setDate(8, cp.getBirthDate() == null ? null : new java.sql.Date(cp.getBirthDate().getTime()));
            ps.setString(9, cp.getWebsite());
            ps.setString(10, cp.getProfession());
            ps.setInt(11, cp.getId());
            //هنا بنملأ آخر ? في الكويري: الـ id.
            //
            //ده اللي بيحدّد أي contact هيتم تحديثه.
            return ps.executeUpdate() > 0;
            //بيرجع عدد الصفوف اللي اتأثرت بالكويري.
            //
            //لو أكبر من 0 → يبقى فيه contact اتحدث → الميثود ترجع true.
            //
            //لو = 0 → يعني مفيش contact بالـ id ده → ترجع false
        } catch (SQLException e) {
            e.printStackTrace(); //لو حصل خطأ SQL، بنطبع الـ stack trace ونرجع false.
            return false;
        }
    }

    // Get all contacts using JDBCRowSet
    //دلوقتي دخلنا في استخدام JdbcRowSet بدل الـ Statement / PreparedStatement العادية.
    //الميثود دي تعتبر نسخة بديلة من getContacts() لكن باستخدام RowSet، واللي بيديك شوية مميزات إضافية
    //الفكرة العامة
    //
    //JdbcRowSet هو Wrapper حوالين ResultSet، لكنه JavaBean يعني:
    //
    //بيقدر يتسلسل (Serializable).
    //
    //بيندمج مع Swing components بسهولة (مفيد لو بتعمل GUI).
    //
    //بيقدر يكون scrollable و updatable بشكل أسهل.
    //
    //الكود ده بيرجع List من ContactPerson عن طريق JdbcRowSet.
    public List<ContactPerson> getAllContactsUsingRowSet() {
        //بنجهز ليست فاضية هتستقبل الكونتاكتس.
        //
        //rowSet لسه مش متعمله initialize.
        List<ContactPerson> contacts = new ArrayList<>();
        JdbcRowSet rowSet = null;

        try {
            // Create JdbcRowSet
            rowSet = RowSetProvider.newFactory().createJdbcRowSet();
            //بننشئ JdbcRowSet باستخدام RowSetProvider.
            //
            //RowSetProvider دي Factory بتوفر لك RowSet بدون ما تحتاج تحدد الـ implementation يدويًا.

            // Use existing connection metadata instead of re-reading properties
            DatabaseMetaData metaData = con.getMetaData();
            rowSet.setUrl(metaData.getURL());
            rowSet.setUsername(metaData.getUserName());
            //بناخد metaData من الـ connection اللي معانا (URL + UserName).
            //
            //نديهم للـ rowSet عشان يعرف إزاي يتصل بالداتابيس.
            //
            //لكن لسه ناقص الـ password.

            // We need to get password from properties just once here
            //هنا بيقرأ الـ password من ملف خصائص DP.properties (في src/).
            //
            //بعدين يحطه في الـ rowSet.
            //
            //لاحظ: .replace("\"", "") دي عشان لو الباسورد متخزن حوالينها علامات تنصيص " "
            Properties props = new Properties();
            //Properties كلاس في جافا بيتعامل مع ملفات الخصائص (.properties).
            //
            //الملفات دي غالبًا بتكون عبارة عن key=value
            FileInputStream fis = new FileInputStream("src/DP.properties");
            //هنا فتحنا الملف DP.properties الموجود جوه فولدر src.
            //
            //FileInputStream بيقرأ الملف كبايتس (stream).
            props.load(fis);
            //بيقرأ محتويات الملف ويخزنها كـ pairs (key, value) جوه كائن props.
            fis.close();
            //دايمًا بعد ما تفتح stream، لازم تقفله.
            //
            //عشان متسيبش resource مفتوح في الذاكرة (best practice).
            rowSet.setPassword(props.getProperty("PASS").replace("\"", ""));

            // Execute the query
            //بنحدد الكويري اللي عايزين نشغله (setCommand).
            //
            //بعدين نشغّله بـ execute().
            //
            //دلوقتي rowSet بقى يحتوي على النتائج (زي ResultSet).
            rowSet.setCommand("SELECT * FROM contact");
            rowSet.execute();

            // Process the results
            while (rowSet.next()) {
                //زي ما عملنا في createContactPerson، بنمشي على النتائج صف صف (rowSet.next()).
                //
                //نعمل أوبجكت ContactPerson ونملاه من أعمدة الجدول.
                //
                //نضيفه لليست.
                ContactPerson cp = new ContactPerson();
                cp.setId(rowSet.getInt("id"));
                cp.setName(rowSet.getString("name"));
                cp.setNickName(rowSet.getString("nick_name"));
                cp.setAddress(rowSet.getString("address"));
                cp.setHomePhone(rowSet.getString("home_phone"));
                cp.setWorkPhone(rowSet.getString("work_phone"));
                cp.setCellPhone(rowSet.getString("cell_phone"));
                cp.setEmail(rowSet.getString("email"));
                cp.setBirthDate(rowSet.getDate("birthday"));
                cp.setWebsite(rowSet.getString("web_site"));
                cp.setProfession(rowSet.getString("profession"));
                contacts.add(cp);
            }

            System.out.println("Retrieved " + contacts.size() + " contacts using JDBCRowSet"); //بنطبع عدد الكونتاكتس اللي جابها.

        } catch (Exception e) {
            //لو حصل أي مشكلة (اتصال/قراءة/Properties) يطبع الخطأ.
            System.err.println("Error in getAllContactsUsingRowSet: " + e.getMessage());
            e.printStackTrace();
        } finally { //يقفل rowSet عشان يحرر الموارد (حتى لو حصل Exception).
            try {
                if (rowSet != null) {
                    rowSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return contacts; //ي الآخر بيرجع ليست فيها كل الكونتاكتس.
    }

    // Get all contacts using WebRowSet and save to XML file
//الكود ده شبه اللي فات بس بدل JdbcRowSet هنا بتستخدم WebRowSet، والميزة الأساسية إنك تقدر تحفظ الداتا في XML file بحيث يبقى عندك نسخة من البيانات برة قاعدة البيانات.
    public List<ContactPerson> getAllContactsUsingWebRowSet(String xml) {
        List<ContactPerson> contacts = new ArrayList<>(); //هنخزن فيها كل الـ ContactPerson اللي هنقراهم من قاعدة البيانات، وبعدين نرجع الليست دي في الآخر.
        WebRowSet webRowSet = null;
        //هنا عرّفنا متغير من نوع WebRowSet، وخليناه في الأول null.
        //
        //ليه null؟ لأننا لسه ما أنشأنّاش الكائن
        FileOutputStream xmlFile = null;
        //برضه هنا عرفنا متغير من نوع FileOutputStream وخليناه null.
        //
        //ده اللي هنستخدمه بعدين عشان نفتح ملف XML ونكتب فيه البيانات.

        try {
            // Create WebRowSet
            webRowSet = RowSetProvider.newFactory().createWebRowSet();
            //هنا بننشئ كائن من نوع WebRowSet.
            //
            //الـ WebRowSet بيشتغل زي الـ JdbcRowSet لكن مع ميزة إضافية:
            //
            //يقدر يقرأ/يكتب البيانات بصيغة XML.

            // Use existing connection metadata
            DatabaseMetaData metaData = con.getMetaData();
            //DatabaseMetaData = كلاس بيديك معلومات عن قاعدة البيانات والاتصال (زي الـ URL، الـ user name، نوع الـ DB، الـ driver... إلخ).
            //
            //con.getMetaData() → بيجيب الميتاداتا من الاتصال اللي عندك (con ده الـ Connection اللي معمول مع قاعدة البيانات).
            //
            //وبكده عندنا object اسمه metaData فيه كل التفاصيل عن الاتصال.
            webRowSet.setUrl(metaData.getURL());
            //هنا إحنا قولنا للـ WebRowSet: استخدم نفس رابط قاعدة البيانات (URL) اللي مستخدمه الاتصال الحالي (con).
            //
            //يعني بدل ما نكتب الـ URL من جديد أو نقراه من ملف، إحنا جبناه مباشرة من الاتصال الموجود.
            //
            //3️⃣
            webRowSet.setUsername(metaData.getUserName());
            //نفس الفكرة، لكن هنا بنحدد للـ WebRowSet اسم المستخدم اللي بيتصل بقاعدة البيانات.
            //
            //metaData.getUserName() بيرجع الـ user name اللي تم استخدامه مع con.
            //
            //بكده الـ WebRowSet بقى عارف يوصل لنفس قاعدة البيانات بنفس المستخدم.

            // Get password from properties
            Properties props = new Properties();
            //هنا أنشأنا object من كلاس Properties.
            //
            //الـ Properties ده عبارة عن خريطة (Map) بيخزن بيانات على شكل key = value.
            //
            //إحنا بنستخدمه علشان نقرأ بيانات من ملف إعدادات (.properties) زي: DB_URL, USER, PASS.
            FileInputStream fis = new FileInputStream("src/DP.properties");
            //هنا فتحنا ملف اسمه DP.properties موجود في المسار src/.
            //
            //FileInputStream بيقرأ الملف كـ "stream" من البايتات علشان نقدر نحمل البيانات منه.
            props.load(fis);
            //هنا حمّلنا البيانات من ملف الخصائص (DP.properties) إلى الـ object props.
            //
            //دلوقتي أي key-value مكتوب في الملف (زي PASS=ab1ab2ab) بقى متخزن جوة props.
            fis.close(); ///قفلنا ملف الإدخال علشان نحرر الموارد (best practice دايمًا).
            webRowSet.setPassword(props.getProperty("PASS").replace("\"", ""));
            //بنبعت الباسورد النظيف للـ webRowSet علشان يستخدمه في الاتصال بقاعدة البيانات

            // Execute the query
            webRowSet.setCommand("SELECT * FROM contact");
            //هنا بنقول للـ WebRowSet إيه الـ SQL query اللي هيشغله.
            webRowSet.execute();
            //نا بنقول له: "يلا نفّذ الاستعلام اللي ادّيناهولك في setCommand".
            //
            //النتيجة:
            //
            //webRowSet بيروح لقاعدة البيانات.
            //
            //ينفّذ الـ SELECT * FROM contact.
            //
            //يخزن البيانات الراجعة (الـ ResultSet) جواه.

            // Save to XML file
            xmlFile = new FileOutputStream("src/" + xml);
            //هنا أنشأنا FileOutputStream جديد.
            //
            //وظيفته إنه يكتب بيانات جوه ملف.
            //
            //"src/" + xml معناها:
            //
            //الملف هيتخزن جوه فولدر src
            //
            //باسم اللي إنت بعتّه في باراميتر xml
            webRowSet.writeXml(xmlFile);
            //هنا بقى بيقول للـ WebRowSet:
            //"اكتب البيانات اللي معاك (اللي رجعت من قاعدة البيانات) جوه الملف ده كـ XML".
            //
            //النتيجة: البيانات كلها اللي في جدول contact بتتسجل في الملف بصيغة XML.
            //
            //ده مفيد جدًا عشان:
            //
            //تخزن نسخة احتياطية.
            //
            //تبعتها لبرنامج تاني يقرأ XML.
            //
            //تشاركها على الإنترنت.
            System.out.println("Data saved to XML file: src/" + xml);

            // Process the results to return as List<ContactPerson>
            webRowSet.beforeFirst(); // Reset cursor to beginning
            //بعد ما WebRowSet نفذ الاستعلام وملّى البيانات، المؤشر (Cursor) بيكون موجود في آخر مكان اتقرا.
            //
            //السطر ده بيرجّع المؤشر لأول الصفوف (قبل أول صف فعلي) عشان نقدر نعمل loop على كل النتائج من الأول.
            while (webRowSet.next()) {
                //هنا بنمشي على الصفوف واحد واحد.
                //
                //next() بيرجع true لو فيه صف جديد، وبيحرك المؤشر عليه.
                ContactPerson cp = new ContactPerson();
                //لكل صف جديد، بنعمل Object جديد من الكلاس ContactPerson.
                //
                //ده عشان نمثل بيانات كل شخص.
                cp.setId(webRowSet.getInt("id"));
                cp.setName(webRowSet.getString("name"));
                cp.setNickName(webRowSet.getString("nick_name"));
                cp.setAddress(webRowSet.getString("address"));
                cp.setHomePhone(webRowSet.getString("home_phone"));
                cp.setWorkPhone(webRowSet.getString("work_phone"));
                cp.setCellPhone(webRowSet.getString("cell_phone"));
                cp.setEmail(webRowSet.getString("email"));
                cp.setBirthDate(webRowSet.getDate("birthday"));
                cp.setWebsite(webRowSet.getString("web_site"));
                cp.setProfession(webRowSet.getString("profession"));
                contacts.add(cp);
            }

            System.out.println("Retrieved " + contacts.size() + " contacts using WebRowSet and saved to XML");

        } catch (Exception e) {
            System.err.println("Error in getAllContactsUsingWebRowSet: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                //هم جدًا عشان نتأكد إننا قفلنا الملفات والـ WebRowSet بعد الاستخدام.
                //
                //ده بيمنع الـ memory leaks أو قفل الـ DB connection.
                if (xmlFile != null) {
                    xmlFile.close();
                }
                if (webRowSet != null) {
                    webRowSet.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contacts;//في الآخر بيرجعلك List<ContactPerson> فيها كل الناس اللي اتجابوا من قاعدة البيانات + اتخزنوا في XML.
    }

    // Load contacts from XML file using WebRowSet
    //ي ميثود بترجع List<ContactPerson>.
    //
    //فكرتها: تقرأ البيانات المخزنة في ملف XML (اللي معمول قبل كده عن طريق getAllContactsUsingWebRowSet) وترجعهم كأوبجيكتات في جافا.
    public List<ContactPerson> loadContactsFromXML(String xml) {
        //contacts → الليست اللي هنحط فيها الأشخاص بعد ما نقرأهم.
        //
        //webRowSet → الكائن اللي هيقرأ XML.
        //
        //xmlFile → يمثل الملف نفسه.
        List<ContactPerson> contacts = new ArrayList<>();
        WebRowSet webRowSet = null;
        FileInputStream xmlFile = null;

        try {
            // Create WebRowSet
            webRowSet = RowSetProvider.newFactory().createWebRowSet();
            //هنا بننشئ WebRowSet جديد (من غير connection للـ DB).
            //
            //ده اللي هيقدر يفهم الـ XML ويقراه كأنه جدول بيانات.

            // Read from XML file
            xmlFile = new FileInputStream("src/" + xml);
            webRowSet.readXml(xmlFile);
            //xmlFile بيشير لملف XML اللي موجود في مجلد src.
            //
            //readXml() بيخلي الـ webRowSet يقرأ محتوى الملف ويحوله لبيانات قابلة للتعامل زي كأنها جاية من DB.

            System.out.println("Data loaded from XML file: src/" + xml);

            // Process the results
            webRowSet.beforeFirst(); // Reset cursor to beginning
            while (webRowSet.next()) {
                //بيرجع المؤشر لأول الصفوف.
                //
                //كل صف في XML بيتحول إلى ContactPerson object.
                //
                //بنضيف الأوبجيكت ده في contacts.
                ContactPerson cp = new ContactPerson();
                cp.setId(webRowSet.getInt("id"));
                cp.setName(webRowSet.getString("name"));
                cp.setNickName(webRowSet.getString("nick_name"));
                cp.setAddress(webRowSet.getString("address"));
                cp.setHomePhone(webRowSet.getString("home_phone"));
                cp.setWorkPhone(webRowSet.getString("work_phone"));
                cp.setCellPhone(webRowSet.getString("cell_phone"));
                cp.setEmail(webRowSet.getString("email"));
                cp.setBirthDate(webRowSet.getDate("birthday"));
                cp.setWebsite(webRowSet.getString("web_site"));
                cp.setProfession(webRowSet.getString("profession"));
                contacts.add(cp);
            }

            System.out.println("Loaded " + contacts.size() + " contacts from XML file");

        } catch (Exception e) {
            System.err.println("Error in loadContactsFromXML: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (xmlFile != null) {
                    xmlFile.close();
                }
                if (webRowSet != null) {
                    webRowSet.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contacts;
    }

    // Batch update emails by specific contact IDs
    //بيعمل Batch Update للإيميلات بتاعت contacts معينة باستخدام الـ IDs بتاعتهم.
    public void batchUpdateEmailsByIds(int[] contactIds, String[] newEmails) {
        //contactIds → مصفوفة IDs للناس اللي عايز تعدّل الإيميل بتاعهم.
        //
        //newEmails → المصفوفة اللي فيها الإيميلات الجديدة بنفس الترتيب.
        if (contactIds.length != newEmails.length) {
            System.out.println("Error: Number of IDs and emails must match!");
            return;
        }

        String sql = "UPDATE contact SET email=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < contactIds.length; i++) {
                ps.setString(1, newEmails[i]);
                ps.setInt(2, contactIds[i]);
                ps.addBatch();
            }

            int[] results = ps.executeBatch(); //executeBatch() بيشغل كل الـ queries اللي اتضافت مرة واحدة.
            System.out.println("Batch update results:");
            for (int i = 0; i < results.length; i++) {
                System.out.println("Contact ID " + contactIds[i] + ": " + results[i] + " rows affected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}