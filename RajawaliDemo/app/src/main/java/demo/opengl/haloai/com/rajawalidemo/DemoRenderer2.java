package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import rajawali.animation.Animation;
import rajawali.animation.Animation3D;
import rajawali.animation.IAnimationListener;
import rajawali.animation.SplineTranslateAnimation3D;
import rajawali.curves.CatmullRomCurve3D;
import rajawali.materials.Material;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Line3D;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/6/22;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer2 extends RajawaliRenderer implements IAnimationListener {
    private final int NUM_POINTS         = 100;
    private final int ANIMATION_DURATION = 5000;
    private final int CURVE1_COLOR       = 0xffffee;
    private final int CURVE2_COLOR       = 0xffffee;

    private List<Vector3> mRoad          = new ArrayList<>();
    private List<Double>  mLine2DestDist = new ArrayList<>();
    private List<Double>  mOffsetX = new ArrayList<>();
    private List<Double>  mOffsetY = new ArrayList<>();

    private SplineTranslateAnimation3D mSplineTranslateAnimation3D = null;
    private int                        mSplineIndex                = 0;

    double mTotalDistance = 0;

    private Sphere mSphere = null;

    public DemoRenderer2(Context context) {
        super(context);

        setFrameRate(100);
    }

    public void initScene() {
        //getCurrentCamera().setPosition(1,2,3);
        //                getCurrentCamera().setZ(2);

        // -- "curve1" will be the original curve. Note that we create two curves for
        // demonstration purposes only. You'd typically create one curve and then
        // reparametrize it.

//        getCurrentCamera().setRotX(-45);

        final CatmullRomCurve3D curve = new CatmullRomCurve3D();

        mRoad.clear();
        for (int i = 0; i < NUM_POINTS; i++) {
            // -- generate a random point within certain limits
            Vector3 pos = null;
            if (i == 0) {
                pos = new Vector3(0, 0, 0);
            } else {
                pos = new Vector3((Math.random() - 0.5) * 0.3, (i + 1) * 0.1, 0);
            }
            curve.addPoint(new Vector3(pos));
            mRoad.add(new Vector3(pos));
        }
//        String strLatLng = "<0.0, 0.0, 0.0>\n" +
//                "<-0.6633084867146977, 0.1012098205688261, 0.0>\n" +
//                "<-0.6677805365562506, -0.9359335087388843, 0.0>\n" +
//                "<-0.667572021484375, -1.1883826608549697, 0.0>\n" +
//                "<-0.6599558032505115, -3.777340948296626, 0.0>\n" +
//                "<-0.9253215780269386, -4.257453950673451, 0.0>\n" +
//                "<-3.1590628322142322, -4.26799968078484, 0.0>\n" +
//                "<-4.00092013617126, -4.290213940771892, 0.0>\n" +
//                "<-5.684257270033299, -4.291644290788099, 0.0>\n" +
//                "<-6.336734478917805, -4.287269341629596, 0.0>\n" +
//                "<-7.533717905339188, -4.347686552286234, 0.0>\n" +
//                "<-11.634094632828607, -4.935193260902082, 0.0>\n" +
//                "<-17.475660417431982, -5.626523676216522, 0.0>\n" +
//                "<-19.25706629837265, -5.805368583233772, 0.0>\n" +
//                "<-20.280642217418432, -5.890467611768813, 0.0>\n" +
//                "<-21.83055017130897, -6.059965363824915, 0.0>\n" +
//                "<-23.169695750979713, -6.333184209106335, 0.0>\n" +
//                "<-24.229491115690394, -6.545997400930759, 0.0>\n" +
//                "<-25.955756930695628, -7.190198416040516, 0.0>\n" +
//                "<-26.999637542513, -7.649055109526515, 0.0>\n" +
//                "<-28.113265304519075, -8.313168932794213, 0.0>\n" +
//                "<-30.709646575921568, -9.815717404082491, 0.0>\n" +
//                "<-31.84240859948062, -10.542045236761055, 0.0>\n" +
//                "<-32.357645811131874, -11.173626444316298, 0.0>\n" +
//                "<-32.5281489041096, -11.274360142321527, 0.0>\n" +
//                "<-32.49448957172163, -11.978989877015067, 0.0>\n" +
//                "<-32.413894024863055, -12.781187622756818, 0.0>\n" +
//                "<-32.28844635154715, -13.56652523729629, 0.0>\n" +
//                "<-32.109471025876246, -14.539498422028174, 0.0>\n" +
//                "<-31.968414312188997, -15.291328496536138, 0.0>\n" +
//                "<-31.66401133274377, -16.32928030552705, 0.0>\n" +
//                "<-31.381197793400872, -17.12150583443872, 0.0>\n" +
//                "<-31.065272032115843, -17.88497938992606, 0.0>\n" +
//                "<-30.52196545345609, -18.987133984360582, 0.0>\n" +
//                "<-28.39308564599463, -23.30285040876845, 0.0>\n" +
//                "<-25.49976567625123, -29.071398566227202, 0.0>\n" +
//                "<-23.583757959322327, -32.835803307440514, 0.0>\n" +
//                "<-22.746855404065514, -34.53266626460838, 0.0>\n" +
//                "<-22.3009732647661, -35.54323465002618, 0.0>\n" +
//                "<-21.854308033297798, -36.673003088694145, 0.0>\n" +
//                "<-21.451184938836576, -37.82646613852414, 0.0>\n" +
//                "<-21.12027761675961, -39.00505526153175, 0.0>\n" +
//                "<-20.866014402276534, -40.19168798139461, 0.0>\n" +
//                "<-20.62568543390597, -41.40714951375024, 0.0>\n" +
//                "<-20.45191641002475, -42.64487878886669, 0.0>\n" +
//                "<-20.365070631900295, -43.53715598961472, 0.0>\n" +
//                "<-20.28027818630207, -43.96584404484649, 0.0>\n" +
//                "<-20.312178293213634, -48.284425666444974, 0.0>\n" +
//                "<-20.458429316150273, -55.90146055034495, 0.0>\n" +
//                "<-20.475317300938656, -58.75296873947278, 0.0>\n" +
//                "<-20.415616885305354, -59.466309590305855, 0.0>\n" +
//                "<-20.46934763653141, -62.31740394064822, 0.0>\n" +
//                "<-20.547660929093325, -68.63730643225097, 0.0>\n" +
//                "<-20.685432465761266, -74.24894144150151, 0.0>\n" +
//                "<-20.738042496493136, -78.90429932202458, 0.0>\n" +
//                "<-20.82230862514578, -80.05840967779676, 0.0>\n" +
//                "<-21.146861071486, -80.64538015633005, 0.0>\n" +
//                "<-21.41630401322203, -81.23897253771162, 0.0>\n" +
//                "<-21.893052291499515, -81.58269536790996, 0.0>\n" +
//                "<-22.39947983557755, -81.85547754995248, 0.0>\n" +
//                "<-22.74350755637755, -81.99882391655144, 0.0>\n" +
//                "<-23.37124496731846, -82.06681759603818, 0.0>\n" +
//                "<-23.646165493307336, -82.0346178694642, 0.0>\n" +
//                "<-23.898322666227045, -81.9553655818428, 0.0>\n" +
//                "<-24.48240391430545, -81.71593692298806, 0.0>\n" +
//                "<-24.69600996652943, -81.52417553861646, 0.0>\n" +
//                "<-25.00631935937747, -81.20030511857834, 0.0>\n" +
//                "<-25.792765701826426, -80.19387453992444, 0.0>\n" +
//                "<-27.698143112253604, -77.63526903405982, 0.0>\n" +
//                "<-28.49277328680344, -77.0186205950818, 0.0>\n" +
//                "<-28.847722712832535, -76.74537172017715, 0.0>\n" +
//                "<-29.274455310090985, -76.58621983899216, 0.0>\n" +
//                "<-29.685539407697803, -76.43119594815673, 0.0>\n" +
//                "<-29.930874770212057, -76.43240090637882, 0.0>\n" +
//                "<-30.81358904735154, -76.3416561082142, 0.0>\n" +
//                "<-34.50153609705353, -76.41484467706405, 0.0>\n" +
//                "<-37.34658674838798, -76.41214868097101, 0.0>\n" +
//                "<-37.57905906791592, -76.36488257567464, 0.0>\n" +
//                "<-37.68006211428698, -76.36579724725756, 0.0>\n" +
//                "<-39.10064697265625, -76.41315460205078, 0.0>\n" +
//                "<-39.77630823985834, -76.24530200160962, 0.0>\n" +
//                "<-45.381759847273884, -76.57430802527898, 0.0>\n" +
//                "<-48.03885557924659, -76.671958871799, 0.0>\n" +
//                "<-49.318269988507524, -76.76486570873386, 0.0>\n" +
//                "<-51.33497387843633, -76.90270426818913, 0.0>\n" +
//                "<-54.45628995914831, -77.11810740942404, 0.0>\n" +
//                "<-56.075354044882175, -77.14994465244374, 0.0>\n" +
//                "<-56.6664022709773, -77.19499731537738, 0.0>\n" +
//                "<-58.75713609233557, -77.24112103630887, 0.0>\n" +
//                "<-61.27473491206459, -77.31107563806461, 0.0>\n" +
//                "<-62.25693827648371, -77.36038835419734, 0.0>\n" +
//                "<-63.21554877262159, -77.40857198408335, 0.0>\n" +
//                "<-64.2311034355636, -77.43059329987645, 0.0>\n" +
//                "<-65.5813271071004, -77.5216178912963, 0.0>\n" +
//                "<-65.73029617706894, -77.5172883009656, 0.0>\n" +
//                "<-69.27425714602364, -77.76334176408106, 0.0>\n" +
//                "<-71.47698158114224, -77.97914786382698, 0.0>\n" +
//                "<-71.98256104130607, -78.0519827500381, 0.0>\n" +
//                "<-72.89605373763663, -78.28247968846291, 0.0>\n" +
//                "<-75.05027741805392, -78.89334275135162, 0.0>\n" +
//                "<-78.17382865340505, -80.08413629587707, 0.0>\n" +
//                "<-82.01325565879714, -81.42536349691198, 0.0>\n" +
//                "<-82.76018325821255, -81.71345268190855, 0.0>\n" +
//                "<-83.88385340305504, -82.09034348967137, 0.0>\n" +
//                "<-86.4291352177915, -82.91600682790445, 0.0>\n" +
//                "<-88.25005761732285, -83.43088049218572, 0.0>\n" +
//                "<-89.24197772870457, -83.64293027303127, 0.0>\n" +
//                "<-90.95117663830976, -83.90544289147428, 0.0>\n" +
//                /*"<-92.54756644461182, -84.0562968666081, 0.0>\n" +
//                "<-96.43606771794211, -84.06109281127172, 0.0>\n" +
//                "<-101.03011513828974, -84.00003873978434, 0.0>\n" +
//                "<-110.84819492408471, -83.76682554356307, 0.0>\n" +
//                "<-117.86532152662232, -83.77465767552827, 0.0>\n" +
//                "<-119.47224463524364, -83.97364421566245, 0.0>\n" +
//                "<-120.38987900702125, -84.11369641065924, 0.0>\n" +
//                "<-121.34898390456783, -84.32519526095916, 0.0>\n" +
//                "<-122.33232414056161, -84.66287473076761, 0.0>\n" +
//                "<-123.64000579312417, -85.22471870087145, 0.0>\n" +
//                "<-124.76844141779964, -85.72676128602019, 0.0>\n" +
//                "<-125.88365799564016, -86.40501380838828, 0.0>\n" +
//                "<-127.41853419449001, -87.55334773411505, 0.0>\n" +
//                "<-128.29295573970612, -88.28703051251274, 0.0>\n" +
//                "<-131.90556625346517, -93.14356110206035, 0.0>\n" +
//                "<-136.02780476711374, -98.86391697375707, 0.0>\n" +
//                "<-136.51807074452904, -99.57795329409436, 0.0>\n" +
//                "<-137.50641447316525, -100.81534097579059, 0.0>\n" +
//                "<-140.55931205874117, -104.15653311826034, 0.0>\n" +
//                "<-145.20946001077562, -109.0936572235357, 0.0>\n" +
//                "<-146.35443235544443, -110.32741842473825, 0.0>\n" +
//                "<-147.32818292637617, -111.37157926497387, 0.0>\n" +
//                "<-149.09187227480913, -113.20425704348658, 0.0>\n" +
//                "<-151.02566693201425, -115.17007898622644, 0.0>\n" +
//                "<-154.165384182825, -118.43878315511702, 0.0>\n" +
//                "<-154.14153067085934, -118.32093066463933, 0.0>\n" +
//                "<-155.2853792968989, -119.33823372123342, 0.0>\n" +
//                "<-156.43642362928034, -120.6121316672526, 0.0>\n" +
//                "<-159.39384488339314, -123.74804641841486, 0.0>\n" +
//                "<-167.84026426881482, -132.4904081076994, 0.0>\n" +
//                "<-173.13956034445965, -138.05075337565142, 0.0>\n" +
//                "<-176.84549917431625, -141.8778187026959, 0.0>\n" +
//                "<-178.04869482347385, -143.1553439995792, 0.0>\n" +
//                "<-184.33114360689729, -149.81876443300735, 0.0>\n" +
//                "<-195.7118714661199, -161.50670500336696, 0.0>\n" +
//                "<-196.44893366939087, -162.30869350848388, 0.0>\n" +
//                "<-199.0237047073684, -165.0053112236094, 0.0>\n" +
//                "<-199.50251482869774, -165.46963118755366, 0.0>\n" +
//                "<-200.5548394328649, -166.6288165559582, 0.0>\n" +
//                "<-201.76144927548378, -167.7887209437401, 0.0>\n" +
//                "<-202.15244831751633, -168.22648380756533, 0.0>\n" +
//                "<-203.34719097991893, -169.48032658263833, 0.0>\n" +
//                "<-211.0905253933204, -177.68971890839146, 0.0>\n" +
//                "<-213.79115823343398, -180.46596488625607, 0.0>\n" +
//                "<-214.33603641263232, -181.0597915569811, 0.0>\n" +
//                "<-219.11618581125225, -185.9557369704223, 0.0>\n" +
//                "<-220.14107133276895, -187.0111524051143, 0.0>\n" +
//                "<-228.79277289700184, -195.9917224174656, 0.0>\n" +
//                "<-230.7297917048423, -197.89962077205732, 0.0>\n" +
//                "<-231.5151202281207, -198.70830864783247, 0.0>\n" +
//                "<-232.41043090820313, -199.50866699218744, 0.0>\n" +
//                "<-235.8088260719704, -202.10543270127164, 0.0>\n" +
//                "<-237.81217532237164, -203.51303038435015, 0.0>\n" +
//                "<-238.45600466607664, -204.03842352081364, 0.0>\n" +
//                "<-240.98772322904694, -206.12063549521048, 0.0>\n" +
//                "<-242.17632920233712, -207.27520108319015, 0.0>\n" +
//                "<-244.4756104563517, -209.45427147873727, 0.0>\n" +
//                "<-245.6559591916374, -210.71841935969002, 0.0>\n" +
//                "<-246.95091349080656, -212.1229276629055, 0.0>\n" +
//                "<-249.60786869749896, -214.53392529853232, 0.0>\n" +
//                "<-250.0860348979117, -214.97408352342262, 0.0>\n" +
//                "<-251.10856060086562, -215.85875727212488, 0.0>\n" +
//                "<-251.77679288796995, -216.56322868617292, 0.0>\n" +
//                "<-253.0794224006705, -217.38998339224656, 0.0>\n" +
//                "<-254.09183869822272, -218.18639764767943, 0.0>\n" +
//                "<-255.90247934834665, -219.617690135232, 0.0>\n" +
//                "<-261.6942784902321, -223.88567042503914, 0.0>\n" +
//                "<-263.3455423992182, -225.08564733954017, 0.0>\n" +
//                "<-271.3194982217004, -230.92052434363632, 0.0>\n" +
//                "<-276.76382488799334, -234.87775408010947, 0.0>\n" +
//                "<-278.507185848742, -236.1587353025829, 0.0>\n" +
//                "<-279.6567907883407, -236.9818898342757, 0.0>\n" +
//                "<-280.9599115108696, -237.91996071973466, 0.0>\n" +
//                "<-283.04437585475733, -239.55372299457878, 0.0>\n" +
//                "<-285.3260445437655, -241.39510033185374, 0.0>\n" +
//                "<-289.9067233976815, -245.34771812194657, 0.0>\n" +
//                "<-293.4162461005307, -248.4300207898027, 0.0>\n" +
//                "<-295.09536177023676, -249.94139009906215, 0.0>\n" +
//                "<-296.5636038449127, -251.36226364845805, 0.0>\n" +
//                "<-297.64855864904405, -252.4346146510759, 0.0>\n" +
//                "<-299.28530918938986, -254.14975517801255, 0.0>\n" +
//                "<-301.57168368551976, -256.5608882614257, 0.0>\n" +
//                "<-302.5728456789167, -257.6581663966682, 0.0>\n" +
//                "<-303.9241140537107, -259.17346016197666, 0.0>\n" +
//                "<-305.68128759442806, -261.0083786077411, 0.0>\n" +
//                "<-306.4945035658794, -261.84536675479825, 0.0>\n" +
//                "<-306.44780062551996, -261.7791837042916, 0.0>\n" +
//                "<-308.2153621077665, -263.6342681550552, 0.0>\n" +
//                "<-310.7242546471766, -266.2592861167657, 0.0>\n" +
//                "<-312.38454466953056, -267.92547689482586, 0.0>\n" +
//                "<-313.82933343424565, -269.3555498203715, 0.0>\n" +
//                "<-316.1424488354214, -271.41400833321507, 0.0>\n" +
//                "<-318.48751318399593, -273.4088881070296, 0.0>\n" +
//                "<-320.648248513411, -275.07453820612153, 0.0>\n" +
//                "<-324.1288839402223, -277.5748281907742, 0.0>\n" +
//                "<-325.63290966782785, -278.557295100379, 0.0>\n" +
//                "<-328.438933076125, -280.41886000997886, 0.0>\n" +
//                "<-332.9746940379685, -283.54228000453395, 0.0>\n" +
//                "<-332.93397304463565, -283.6680135159142, 0.0>\n" +
//                "<-334.4079030562774, -284.63380554981654, 0.0>\n" +
//                "<-337.40858510684643, -286.64815073327077, 0.0>\n" +
//                "<-339.8823536788775, -288.5136938716837, 0.0>\n" +
//                "<-341.4530624676892, -289.676107791248, 0.0>\n" +
//                "<-345.1988228516889, -292.59539485826383, 0.0>\n" +
//                "<-353.6728403171096, -299.6471509785881, 0.0>\n" +
//                "<-355.63708737109675, -301.4283290983411, 0.0>\n" +
//                "<-358.8501763187146, -304.35380512856557, 0.0>\n" +
//                "<-360.53296769615883, -306.0509134005241, 0.0>\n" +
//                "<-363.6648517996832, -309.316036786278, 0.0>\n" +
//                "<-365.2079691816468, -311.017586353124, 0.0>\n" +
//                "<-367.0142905896512, -313.03354878611253, 0.0>\n" +
//                "<-368.2922186031945, -314.5183929108342, 0.0>\n" +
//                "<-369.4730007134161, -316.0249448534103, 0.0>\n" +
//                "<-370.98794505139307, -317.92577666300843, 0.0>\n" +
//                "<-372.27303782279006, -319.59388542239793, 0.0>\n" +
//                "<-373.5114690472052, -321.2951663665199, 0.0>\n" +
//                "<-376.04535313082715, -324.9751389212908, 0.0>\n" +
//                "<-377.3414367580052, -326.9590114463484, 0.0>\n" +
//                "<-378.8969279074905, -329.4527275466309, 0.0>\n" +
//                "<-381.1784312157229, -333.38570347249976, 0.0>\n" +
//                "<-382.5499928709952, -335.8805663576271, 0.0>\n" +
//                "<-383.2630235120149, -337.25234890749095, 0.0>\n" +
//                "<-383.3692495915031, -337.48617879973983, 0.0>\n" +
//                "<-384.9142553121965, -340.5093288392893, 0.0>\n" +
//                "<-385.3809027923205, -341.59216614133595, 0.0>\n" +
//                "<-388.972978434321, -349.5304260303647, 0.0>\n" +
//                "<-389.9420106251661, -351.64463656629533, 0.0>\n" +
//                "<-390.657282223561, -353.31433718792687, 0.0>\n" +
//                "<-392.46826359651266, -357.38791183894693, 0.0>*/"";
//
//        strLatLng = strLatLng.replace("<","");
//        strLatLng = strLatLng.replace(">","");
//        strLatLng = strLatLng.replace(" ","");
//        String[] strLatLngs = strLatLng.split("\n");
//        mRoad.clear();
//        mOffsetX.clear();
//        mOffsetY.clear();
//        for (int i = 0; i < strLatLngs.length; i++) {
//            String[] xy = strLatLngs[i].split(",");
//            double x = Double.parseDouble(xy[0].trim());
//            double y = Double.parseDouble(xy[1].trim());
//            double z = Double.parseDouble(xy[0].trim());
//            mRoad.add(new Vector3(x,y,z));
//
//            if(i!=0){
//                mOffsetX.add(x-mRoad.get(mRoad.size()-2).x);
//                mOffsetY.add(y-mRoad.get(mRoad.size()-2).y);
//            }
//        }
//
//        //bigger
//        for(int i=1;i<mRoad.size();i++){
//            mRoad.get(i).x=mRoad.get(i-1).x+mOffsetX.get(i-1)*2;
//            mRoad.get(i).y=mRoad.get(i-1).y+mOffsetY.get(i-1)*2;
//        }

        mLine2DestDist.clear();
        mTotalDistance = 0;
        for (int i = 0; i < this.mRoad.size() - 1; i++) {
            mTotalDistance += MathUtils.calculateDistance(this.mRoad.get(i), this.mRoad.get(i + 1));
        }
        for (int i = 0; i < this.mRoad.size(); i++) {
            double currentLine2Dist = 0;
            if (i == 0) {
                currentLine2Dist = mTotalDistance;
            } else {
                currentLine2Dist = mLine2DestDist.get(i - 1) - MathUtils.calculateDistance(this.mRoad.get(i), this.mRoad.get(i - 1));
            }
            mLine2DestDist.add(currentLine2Dist);
        }
        mLine2DestDist.remove(mLine2DestDist.size() - 1);
        mLine2DestDist.add(0.0);

        for (int i = 0; i < mLine2DestDist.size(); i++) {
            double dist = mLine2DestDist.remove(i);
            mLine2DestDist.add(i, dist / mTotalDistance);
        }

        // -- draw the second, reparametrized, curve
        drawCurve(mRoad, CURVE2_COLOR, new Vector3());

        //move();

        new Thread() {
            @Override
            public void run() {
                while (true && speedIndex < speeds.length) {
                    SPEED = speeds[speedIndex++];
                    SystemClock.sleep(1500);
                }
            }
        }.start();
    }

    int    speedIndex = 0;
    long   SPEED      = 40000;
    long[] speeds     = new long[]{35000, 60000, 18000, 40000, 23000, 88000, 24000, 57000, 11000, 43000, 98000, 111000, 40000};
//    long[] speeds     = new long[]{45000, 40000, 48000, 40000, 43000, 48000, 44000, 47000, 41000, 43000, 48000, 41000, 40000};

    CatmullRomCurve3D mCameraCurve = null;
    Animation3D       cameraAnim   = null;

    private void move() {
        mCameraCurve = new CatmullRomCurve3D();
        for (int i = 0; i < this.mRoad.size(); i++) {
            // TODO: 2016/6/27 另外,供该动画使用的CatmullRomCurve3D最少需要添加四个点
            // TODO: 2016/6/27 暂时不清楚为什么需要add两次
            mCameraCurve.addPoint(new Vector3(this.mRoad.get(i).x, this.mRoad.get(i).y, 2));
            //            if(i==this.mRoad.size()-1){
            //                mCameraCurve.addPoint(new Vector3(this.mRoad.get(i).x, this.mRoad.get(i).y, 2));
            //            }
        }

        drawCurve(mRoad,0xff0000,new Vector3());

        /*cameraAnim = new SplineTranslateAnimation3D(mCameraCurve);
        cameraAnim.setDurationMilliseconds(SPEED);
        cameraAnim.setTransformable3D(getCurrentCamera());
        cameraAnim.setInterpolator(new LinearInterpolator());
        cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        getCurrentScene().registerAnimation(cameraAnim);
        cameraAnim.registerListener(this);
        cameraAnim.play();*/
    }

    Stack<Vector3> mTemp = new Stack<>();
    Stack<Vector3> mLine = new Stack<>();
    Stack<Vector3> mLine1 = new Stack<>();
    Stack<Vector3> mLine2 = new Stack<>();
    private Line3D mLine3D = null;
    private Line3D mLine3D1 = null;
    private Line3D mLine3D2 = null;
    private void drawCurve(List<Vector3> roads, int color, Vector3 position) {
        Material lineMaterial = new Material();

        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        for(int i=0;i<roads.size();i++){
            catmull.addPoint(new Vector3(roads.get(i)));
        }

        mTemp.clear();
        mTemp.addAll(roads);
        mLine.clear();
        for (int i = 0; i < roads.size()*50; i++) {
            Vector3 pos = new Vector3();
            catmull.calculatePoint(pos,(1.0*i)/(1.0*roads.size()*50));
//            Vector3 pos = roads.get(i);
            mLine.add(new Vector3(pos));
        }

        mLine3D = new Line3D(mLine, 2, color);
        mLine3D.setMaterial(lineMaterial);
        mLine3D.setPosition(position);
        getCurrentScene().addChild(mLine3D);

        mLine1.clear();
        mLine2.clear();
        MathUtils.points2path(mLine1,mLine2,mLine,0.01);

//        Material material = new Material();
//        try {
//            material.addTexture(new Texture("route",R.mipmap.route));
//        } catch (ATexture.TextureException e) {
//            e.printStackTrace();
//        }
//        material.setColorInfluence(0);

//        for(int i=0;i<(mLine.size()-1)/10;i++){
//            Vector3 v1 = mLine.get(i);
//            Vector3 v2 = mLine.get(i+1);
//            Plane plane = new Plane(0.5f, (float)(v2.y-v1.y), 24, 24);
//            plane.setPosition((v1.x+v2.x)/2,(v1.y+v2.y)/2,0);
//            plane.setMaterial(material);
//            plane.setDoubleSided(true);
//            plane.setColor(0xff3333ff);
//            plane.setRotZ(MathUtils.getDegrees(v1.x,v1.y,v2.x,v2.y));
//            getCurrentScene().addChild(plane);
//        }

        for(int i=0;i<mLine1.size();i++){
            mLine1.get(i).x-=0.3;
        }
        mLine3D1 = new Line3D(mLine1, 5, color);
        mLine3D1.setMaterial(lineMaterial);
        mLine3D1.setPosition(position);
        getCurrentScene().addChild(mLine3D1);

        for(int i=0;i<mLine2.size();i++){
            mLine2.get(i).x+=0.3;
        }
        mLine3D2 = new Line3D(mLine2, 5, color);
        mLine3D2.setMaterial(lineMaterial);
        mLine3D2.setPosition(position);
        getCurrentScene().addChild(mLine3D2);

        //test组合曲线动画
        CatmullRomCurve3D compoundCurve3D = new CatmullRomCurve3D();
        for (int i = 0; i < mLine.size(); i++) {
           compoundCurve3D.addPoint(new Vector3(mLine.get(i).x,mLine.get(i).y,2));
        }
        cameraAnim = new SplineTranslateAnimation3D(compoundCurve3D);
        cameraAnim.setDurationMilliseconds(100000);
        cameraAnim.setTransformable3D(getCurrentCamera());
        cameraAnim.setInterpolator(new LinearInterpolator());
        cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        cameraAnim.registerListener(this);
        getCurrentScene().registerAnimation(cameraAnim);
//        cameraAnim.setOrientToPath(true);
        cameraAnim.play();

        /*ChaseCamera chaseCamera = new ChaseCamera(new Vector3(-1,0,0),0.1f);
        chaseCamera.setObjectToChase(mSphere);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(),chaseCamera);*/

        /*CatmullRomCurve3D catmullRomCurve3D = new CatmullRomCurve3D();
        for (int i = 0; i < mTemp.size(); i++) {
            catmullRomCurve3D.addPoint(new Vector3(mTemp.get(i)));
        }
        cameraAnim = new SplineTranslateAnimation3D(catmullRomCurve3D);
        cameraAnim.setDurationMilliseconds(50000);
        cameraAnim.setTransformable3D(mSphere);
        cameraAnim.setInterpolator(new LinearInterpolator());
        cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        cameraAnim.registerListener(this);
        getCurrentScene().registerAnimation(cameraAnim);
        cameraAnim.play();*/

//        Material material = new Material();
//        material.enableLighting(true);
//        material.setDiffuseMethod(new DiffuseMethod.Lambert());
//        Cube cube = new Cube(0.1f);
//        cube.setColor(0xff00ff);
//        cube.setMaterial(material);
//        cube.setPosition(new Vector3());
//        getCurrentScene().addChild(cube);
//        cameraAnim = new SplineTranslateAnimation3D(compoundCurve3D);
//        cameraAnim.setDurationMilliseconds(100000);
//        cameraAnim.setTransformable3D(cube);
//        cameraAnim.setInterpolator(new LinearInterpolator());
//        cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
//        getCurrentScene().registerAnimation(cameraAnim);
//        cameraAnim.play();



        //        mLine3D = new Line3D(line1, 2, color);
        //        mLine3D.setMaterial(lineMaterial);
        //        mLine3D.setPosition(position);
        //        getCurrentScene().addChild(mLine3D);
        //
        //        mLine3D = new Line3D(line2, 2, color);
        //        mLine3D.setMaterial(lineMaterial);
        //        mLine3D.setPosition(position);
        //        getCurrentScene().addChild(mLine3D);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

//        getCurrentCamera().setY(getCurrentCamera().getY()-1);
//        getCurrentCamera().setZ(0.2);

        //getCurrentCamera().setZ(getCurrentCamera().getZ()+0.1);

        /*if(mFrameCount++==200) {
            if (cameraAnim.isPlaying()) {
                Log.e("helong_debug", "isPlaying:pause");
                cameraAnim.pause();
            } else {
                Log.e("helong_debug", "isPause:play");
                cameraAnim.play();
            }
            mFrameCount = 0;
        }*/
        /**
         * 其实本质上就是移动摄像机的x,y,以及在x,z方向做旋转
         * x,y主要是体现车的移动与旋转
         * x方向旋转体现透视
         * z方向旋转体现车头在转动*/


        //        calcNeedFrameCount();
        //        mFrameCount++;
        //        if (mFrameCount < mIndexNeedFrameCount) {
        //            getCurrentCamera().setX(getCurrentCamera().getX() + getCameraX());
        //            getCurrentCamera().setY(getCurrentCamera().getY() + getCameraY());
        //        } else {
        //            mCurrentIndex++;
        //            mFrameCount = 0;
        //            mIndexNeedFrameCount = 0;
        //        }

        //Log.e("helong_debug", "on frame");
    }

    /**
     * 模拟ARWay移动,匀速
     * 每次0.1
     */

    int mFrameCount          = 0;
    //0-1
    int mCurrentIndex        = 0;
    int mIndexNeedFrameCount = 0;

    private void calcNeedFrameCount() {
        if (mIndexNeedFrameCount == 0 && mCurrentIndex + 1 < mRoad.size()) {
            Vector3 v1 = mRoad.get(mCurrentIndex);
            Vector3 v2 = mRoad.get(mCurrentIndex + 1);
            double dist = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            mIndexNeedFrameCount = (int) (dist / 0.02 + 1);
        }
    }

    private double getCameraX() {
        Vector3 v1 = mRoad.get(mCurrentIndex);
        Vector3 v2 = mRoad.get(mCurrentIndex + 1);
        //Log.e("helong_debug","camearX : "+(v2.x - v1.x) * (1.0*mFrameCount / mIndexNeedFrameCount));
        return (v2.x - v1.x) * (1.0 / mIndexNeedFrameCount);
    }

    private double getCameraY() {
        Vector3 v1 = mRoad.get(mCurrentIndex);
        Vector3 v2 = mRoad.get(mCurrentIndex + 1);
        //Log.e("helong_debug", "camearY : "+(v2.y - v1.y) * (1.0*mFrameCount / mIndexNeedFrameCount));
        return (v2.y - v1.y) * (1.0 / mIndexNeedFrameCount);
    }

    private double getRotZ() {
        Vector3 v1 = mRoad.get(mCurrentIndex);
        Vector3 v2 = mRoad.get(mCurrentIndex + 1);
        //Log.e("helong_debug","rotZ : "+MathUtils.getDegrees(v1.x,v1.y,v2.x,v2.y));
        return MathUtils.getDegrees(v1.x, v1.y, v2.x, v2.y);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //Log.e("helong_debug", "end============");
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        //Log.e("helong_debug", "repeat--------------");
    }

    @Override
    public void onAnimationStart(Animation animation) {
        //Log.e("helong_debug", "start==============");
    }

    int count = 0;

    @Override
    public void onAnimationUpdate(Animation animation, double v) {
//        getCurrentCamera().setY(getCurrentCamera().getY()-1);
//        getCurrentCamera().setZ(0.2);
//        mSphere.setPosition(getCurrentCamera().getX(),getCurrentCamera().getY(),0);
//        animation.setDurationMilliseconds(SPEED);
//        animation.pause();
//        if (count++ == 200) {
//            count = 0;
//            animation.setDurationMilliseconds(SPEED);
//            Log.e("helong_debug", "" + animation.getDurationMilliseconds());
//        }
        /*if(new Random().nextInt()%2==0){
            animation.setDurationMilliseconds(animation.getDurationMilliseconds()-50);
        }else{
            animation.setDurationMilliseconds(animation.getDurationMilliseconds()+50);
        }*/
        count++;
//        Log.e("helong_debug", "camera_position:"+getCurrentCamera().getPosition());
    }/*{
        //动画数据更新,例如属性从0变化到100,v表示0.0-1.0,0.0表示还未变化,1.0表示变化到终点了
        //该回调的调用频率为每一帧都会回调一次
        //通过mCameraCurve.getPoint(i)得到的point就是用于做动画的集合中的vector3
        //Log.e("helong_debug", "update:" + v);
        if (SPEED != 0 && testSpeed != SPEED) {
            animation.pause();
            //Log.e("helong_debug", "pause");
            testSpeed = SPEED;
            mTotalDistance = mTotalDistance * (1 - v);
            //Log.e("helong_debug", "pause totalDist:" + mTotalDistance);
            //Log.e("helong_debug", "pause SPEED:" + SPEED);
            //Log.e("helong_debug", "pause duration:" + ((long) (mTotalDistance / testSpeed)));
            //Vector3 v1 = mCameraCurve.getPoint(0);
            //Vector3 v2 = mCameraCurve.getPoint(2);
            //Vector3 startV = new Vector3(v1.x + (v2.x - v1.x) * v, v1.y + (v2.y - v1.y) * v, v1.z + (v2.z - v1.z) * v);
            //move(startV, (long) (mTotalDistance/testSpeed));
            //animation.setDurationMilliseconds((long) *//*(mTotalDistance/testSpeed)*//*40000);
            //animation.play();

            for (int i = 0; i < this.mLine2DestDist.size(); i++) {
                double curScale = this.mLine2DestDist.get(i);
                if (curScale < 1 - v) {
                    Vector3 v1 = this.mRoad.get(i - 1);
                    Vector3 v2 = this.mRoad.get(i);
                    double div = ((1 - v) - (this.mLine2DestDist.get(i - 1))) / (this.mLine2DestDist.get(i) - this.mLine2DestDist.get(i - 1));
                    Vector3 startV = new Vector3(v1.x + (v2.x - v1.x) * div, v1.y + (v2.y - v1.y) * div, v1.z + (v2.z - v1.z) * div);

                    Log.e("helong_debug", "v1:" + v1 + ",v1.scale:" + this.mLine2DestDist.get(i - 1));
                    Log.e("helong_debug", "v2:" + v2 + ",v2.scale:" + this.mLine2DestDist.get(i));
                    Log.e("helong_debug", "div:" + div);
                    Log.e("helong_debug", "1-v:" + (1 - v));
                    Log.e("helong_debug", "startV:" + startV);
                    Log.e("helong_debug", "====================================================");

                    mCameraCurve = new CatmullRomCurve3D();
                    mCameraCurve.addPoint(startV);
                    for (int j = i; j < this.mRoad.size(); j++) {
                        mCameraCurve.addPoint(new Vector3(this.mRoad.get(j)));
                        if (j == this.mRoad.size() - 1) {
                            mCameraCurve.addPoint(new Vector3(this.mRoad.get(j)));
                        }
                    }

                    Animation3D cameraAnim = new SplineTranslateAnimation3D(mCameraCurve);
                    cameraAnim.setDurationMilliseconds((long) (mTotalDistance / testSpeed));
                    cameraAnim.setTransformable3D(getCurrentCamera());
                    cameraAnim.setInterpolator(new LinearInterpolator());
                    cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
                    getCurrentScene().registerAnimation(cameraAnim);
                    cameraAnim.registerListener(this);
                    cameraAnim.play();

                    break;
                }
            }
        }
    }

    double testSpeed = 0;*/

    public void pause() {
        if(cameraAnim!=null && cameraAnim.isPlaying()){
            cameraAnim.pause();
        }
    }

    public void continue_() {
        if(cameraAnim!=null && cameraAnim.isPaused()){
            cameraAnim.play();
        }
    }
}