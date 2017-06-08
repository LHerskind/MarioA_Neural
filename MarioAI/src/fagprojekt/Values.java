package fagprojekt;

public class Values {

	public static int penaltyLoseLife = 500;
	public static int penaltyDie = 2000;
	public static int penaltyGap = 2000;

	public static int parentToPunishLoseLife = 0; // 5;
	public static int parentToPunishDie = 0;// 6;
	public static int parentToPunishGap = 0;// 6;

	public static boolean old = false;

	public static int getPPLL(int i) {
		if (!old) {
			return (int) (penaltyLoseLife / Math.pow(4, i));
		}
		return (int) (50 / Math.pow(i + 1, 3));
	}

	public static int getPPD(int i) {
		if (!old) {
			return (int) (penaltyDie / Math.pow(4, i));
		}
		return (int) (100 / Math.pow(i + 1, 3));
	}

	public static int getPPG(int i) {
		if (!old) {
			return (int) (penaltyGap / Math.pow(4, i));
		}
		return (int) (2000 / Math.pow(i * 2, 3));
	}

}

// NOTHING = 2
// Old = 2

