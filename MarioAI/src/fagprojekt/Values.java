package fagprojekt;

public class Values {

	public static int penaltyLoseLife = 500;
	public static int penaltyDie = 2000;
	public static int penaltyGap = 2000;

	public static int parentToPunishLoseLife = 3;
	public static int parentToPunishDie = 3;
	public static int parentToPunishGap = 6; // 5

	public static int getPPLL(int i) {
		return (int) (50 / Math.pow(i + 1, 3));
	}

	public static int getPPD(int i) {
		return (int) (100 / Math.pow(i + 1, 3));
	}

	public static int getPPG(int i) {
		return (int) (2000 / Math.pow(i * 2, 3));
	}

}





//public static int parentToPunishLoseLife = 3;
//public static int parentToPunishDie = 3;
//public static int parentToPunishGap = 3; // 5
//
//public static int getPPLL(int i) {
//	return (int) (50 / Math.pow(i + 1, 3));
//}
//
//public static int getPPD(int i) {
//	return (int) (200 / Math.pow(i + 1, 3));
//}
//
//public static int getPPG(int i) {
//	return (int) (2000 / Math.pow(i * 2, 3));
//}