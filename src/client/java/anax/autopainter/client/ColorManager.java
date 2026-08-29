package anax.autopainter.client;

import java.awt.Color;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

class BasicColor {
	int id;
	public Item dye;
	public int index;

	public BasicColor(int id, Item dye){
		this.id = id;
		this.dye = dye;
	}

	static BasicColor
			VOID  				= new BasicColor(0, Items.ENDER_EYE),
			GRASS 				= new BasicColor(1, Items.SHORT_GRASS),
			CREAM 				= new BasicColor(2, Items.PUMPKIN_SEEDS),
			LIGHT_GRAY 			= new BasicColor(3, Items.COBWEB),
			RED 				= new BasicColor(4, Items.DYE.red()),
			ICE 				= new BasicColor(5, Items.ICE), 
			SILVER 				= new BasicColor(6, Items.DYE.lightGray()),
			LEAVES 				= new BasicColor(7, Items.OAK_LEAVES),
			SNOW 				= new BasicColor(8, Items.SNOW), 
			GRAY 				= new BasicColor(9, Items.DYE.gray()),
			COFFEE 				= new BasicColor(10, Items.MELON_SEEDS),
			STONE 				= new BasicColor(11, Items.GHAST_TEAR), 
			WATER 				= new BasicColor(12, Items.LAPIS_BLOCK), 
			DARK_WOOD			= new BasicColor(13, Items.DARK_OAK_LOG),
			WHITE 				= new BasicColor(14, Items.BONE_MEAL),
			ORANGE 				= new BasicColor(15, Items.DYE.orange()),
			MAGENTA 			= new BasicColor(16, Items.DYE.magenta()),
			LIGHT_BLUE 			= new BasicColor(17, Items.DYE.lightBlue()),
			YELLOW 				= new BasicColor(18, Items.DYE.yellow()),
			LIME 				= new BasicColor(19, Items.DYE.lime()),
			PINK 				= new BasicColor(20, Items.DYE.pink()),
			GRAPHITE 			= new BasicColor(21, Items.FLINT),
			GUNPOWDER			= new BasicColor(22, Items.GUNPOWDER),
			CYAN 				= new BasicColor(23, Items.DYE.cyan()),
			PURPLE 				= new BasicColor(24, Items.DYE.purple()),
			BLUE 				= new BasicColor(25, Items.LAPIS_LAZULI),
			BROWN 				= new BasicColor(26, Items.COCOA_BEANS),
			GREEN 				= new BasicColor(27, Items.DYE.green()),
			BRICK				= new BasicColor(28, Items.BRICK), 
			BLACK 				= new BasicColor(29, Items.INK_SAC),
			GOLD 				= new BasicColor(30, Items.GOLD_NUGGET),
			AQUA 				= new BasicColor(31, Items.PRISMARINE_CRYSTALS),
			LAPIS 				= new BasicColor(32, Items.LAPIS_ORE), 
			EMERALD 			= new BasicColor(33, Items.EMERALD), 
			LIGHT_WOOD 			= new BasicColor(34, Items.BIRCH_WOOD),
			MAROON 				= new BasicColor(35, Items.NETHER_WART),
			WHITE_TERRACOTTA 	= new BasicColor(36, Items.EGG), 
			ORANGE_TERRACOTTA 	= new BasicColor(37, Items.MAGMA_CREAM), 
			MAGENTA_TERRACOTTA 	= new BasicColor(38, Items.BEETROOT), 
			LIGHT_BLUE_TERRACOTTA = new BasicColor(39, Items.MYCELIUM),
			YELLOW_TERRACOTTA 	= new BasicColor(40, Items.GLOWSTONE_DUST), 
			LIME_TERRACOTTA 	= new BasicColor(41, Items.SLIME_BALL), 
			PINK_TERRACOTTA 	= new BasicColor(42, Items.SPIDER_EYE), 
			GRAY_TERRACOTTA 	= new BasicColor(43, Items.SOUL_SAND), 
			LIGHT_GRAY_TERRACOTTA = new BasicColor(44, Items.BROWN_MUSHROOM), 
			CYAN_TERRACOTTA 	= new BasicColor(45, Items.IRON_NUGGET), 
			PURPLE_TERRACOTTA 	= new BasicColor(46, Items.CHORUS_FRUIT), 
			BLUE_TERRACOTTA 	= new BasicColor(47, Items.PURPUR_BLOCK), 
			BROWN_TERRACOTTA 	= new BasicColor(48, Items.PODZOL),
			GREEN_TERRACOTTA 	= new BasicColor(49, Items.POISONOUS_POTATO), 
			RED_TERRACOTTA 		= new BasicColor(50, Items.APPLE), 
			BLACK_TERRACOTTA 	= new BasicColor(51, Items.CHARCOAL),
			CRIMSON_NYLIUM 		= new BasicColor(52, Items.CRIMSON_NYLIUM),
			CRIMSON_STEM 		= new BasicColor(53, Items.CRIMSON_STEM),
			CRIMSON_HYPHAE 		= new BasicColor(54, Items.CRIMSON_HYPHAE),
			WARPNED_NYLIUM 		= new BasicColor(55, Items.WARPED_NYLIUM),
			WARPED_STEM 		= new BasicColor(56, Items.WARPED_STEM),
			WARPED_HYPHAE 		= new BasicColor(57, Items.WARPED_HYPHAE),
			WARPED_WART_BLOCK 	= new BasicColor(58, Items.WARPED_WART_BLOCK),
			DEEPSLATE 			= new BasicColor(59, Items.COBBLED_DEEPSLATE),
			RAW_IRON 			= new BasicColor(60, Items.RAW_IRON),
			GLOW_LICHEN 		= new BasicColor(61, Items.GLOW_LICHEN);

	public static BasicColor[] colors = new BasicColor[] { BLACK, RED, GREEN, BROWN, BLUE, PURPLE, CYAN, SILVER, GRAY, PINK, LIME,
			YELLOW, LIGHT_BLUE, MAGENTA, ORANGE, WHITE, CREAM, COFFEE, GRAPHITE, GUNPOWDER, MAROON, AQUA, GRASS, GOLD,
			VOID, LIGHT_GRAY, ICE, LEAVES, SNOW, STONE, WATER, DARK_WOOD, BRICK, LAPIS, EMERALD, LIGHT_WOOD,
			WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, YELLOW_TERRACOTTA,
			LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA,
			PURPLE_TERRACOTTA, BLUE_TERRACOTTA, BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
			CRIMSON_NYLIUM, CRIMSON_STEM, CRIMSON_HYPHAE, WARPNED_NYLIUM, WARPED_STEM, WARPED_HYPHAE, WARPED_WART_BLOCK,
			DEEPSLATE, RAW_IRON, GLOW_LICHEN

	};

	static{
		for(int i = 0; i < colors.length; i++){
			colors[i].index = i;
		}
	}

}

class DyeColor{
	Color color;
	BasicColor base;
	int shift;
	int mcid;

	DyeColor(BasicColor base, int shade){
		this.base = base;
		this.mcid = (base.id * 4 + shade);
		this.color = mcPalette[this.mcid];

		switch (shade) {
			case 0:
				shift = -1;
				break;
			case 1:
				shift = 0;
				break;
			case 2:
				shift = 1;
				break;
			case 3:
				shift = -2;
				break;
		}

	}

	@Override
	public boolean equals(Object o){
		if(o instanceof DyeColor other){
			return color.equals(other.color);
		}
		return false;
	}

	private static DyeColor[] possibleColors = null;

	public static DyeColor[] getPossibleColors(){
		if(possibleColors == null){
			possibleColors = new DyeColor[BasicColor.colors.length * 4];
			for(BasicColor bc : BasicColor.colors){
				for(int shade = 0; shade < 4; shade++){
					DyeColor color = new DyeColor(bc, shade);
					possibleColors[color.mcid] = color;
				}
			}
		}
		return possibleColors;
	}


	public static DyeColor unshaded(BasicColor base){
		return getPossibleColors()[base.id * 4 + 1];
	}

	public static Color[] mcPalette = new Color[] {
		new Color(0, 0, 0, 0),
		new Color(0, 0, 0, 0),
		new Color(0, 0, 0, 0),
		new Color(0, 0, 0, 0),
		new Color(5864743),
		new Color(7182640),
		new Color(8368696),
		new Color(4415005),
		new Color(11445363),
		new Color(14010764),
		new Color(16247203),
		new Color(8551254),
		new Color(9211020),
		new Color(11250603),
		new Color(13092807),
		new Color(6908265),
		new Color(11796480),
		new Color(14417920),
		new Color(16711680),
		new Color(8847360),
		new Color(7368884),
		new Color(9079516),
		new Color(10526975),
		new Color(5526663),
		new Color(7697781),
		new Color(9474192),
		new Color(10987431),
		new Color(5789784),
		new Color(22272),
		new Color(27136),
		new Color(31744),
		new Color(16640),
		new Color(11842740),
		new Color(14474460),
		new Color(16777215),
		new Color(8882055),
		new Color(7566977),
		new Color(9277598),
		new Color(10791096),
		new Color(5658721),
		new Color(6966326),
		new Color(8543810),
		new Color(9923917),
		new Color(5191976),
		new Color(5197647),
		new Color(6316128),
		new Color(7368816),
		new Color(3881787),
		new Color(2960820),
		new Color(3618780),
		new Color(4210943),
		new Color(2171271),
		new Color(6575154),
		new Color(8087102),
		new Color(9402184),
		new Color(4931366),
		new Color(11841964),
		new Color(14473683),
		new Color(16776437),
		new Color(8881537),
		new Color(9984292),
		new Color(12217644),
		new Color(14188339),
		new Color(7488283),
		new Color(8205720),
		new Color(10043834),
		new Color(11685080),
		new Color(6170738),
		new Color(4746392),
		new Color(5801146),
		new Color(6724056),
		new Color(3559794),
		new Color(10592548),
		new Color(12961068),
		new Color(15066419),
		new Color(7960859),
		new Color(5869585),
		new Color(7188501),
		new Color(8375321),
		new Color(4418573),
		new Color(11164020),
		new Color(13659534),
		new Color(15892389),
		new Color(8405847),
		new Color(3487029),
		new Color(4276545),
		new Color(5000268),
		new Color(2631720),
		new Color(7105644),
		new Color(8684676),
		new Color(10066329),
		new Color(5329233),
		new Color(3496300),
		new Color(4287876),
		new Color(5013401),
		new Color(2638673),
		new Color(5844093),
		new Color(7157401),
		new Color(8339378),
		new Color(4399454),
		new Color(2372989),
		new Color(2900377),
		new Color(3361970),
		new Color(1779806),
		new Color(4732196),
		new Color(5783852),
		new Color(6704179),
		new Color(3549211),
		new Color(4741412),
		new Color(5795116),
		new Color(6717235),
		new Color(3556123),
		new Color(7087140),
		new Color(8662060),
		new Color(10040115),
		new Color(5315355),
		new Color(1118481),
		new Color(1381653),
		new Color(1644825),
		new Color(855309),
		new Color(11577398),
		new Color(14142786),
		new Color(16445005),
		new Color(8683048),
		new Color(4233878),
		new Color(5225655),
		new Color(6085589),
		new Color(3175280),
		new Color(3431092),
		new Color(4157148),
		new Color(4882687),
		new Color(2573191),
		new Color(39208),
		new Color(47922),
		new Color(55610),
		new Color(29214),
		new Color(5979170),
		new Color(7293482),
		new Color(8476209),
		new Color(4467993),
		new Color(5177600),
		new Color(6291712),
		new Color(7340544),
		new Color(3866880),
		new Color(9665649),
		new Color(11835530),
		new Color(13742497),
		new Color(7232853),
		new Color(7354649),
		new Color(8996383),
		new Color(10441252),
		new Color(5516051),
		new Color(6896972),
		new Color(8407901),
		new Color(9787244),
		new Color(5123641),
		new Color(5196897),
		new Color(6315383),
		new Color(7367818),
		new Color(3881289),
		new Color(8609049),
		new Color(10514975),
		new Color(12223780),
		new Color(6440467),
		new Color(4739621),
		new Color(5792813),
		new Color(6780213),
		new Color(3554588),
		new Color(7353911),
		new Color(9060931),
		new Color(10505550),
		new Color(5515305),
		new Color(2628632),
		new Color(3220254),
		new Color(3746083),
		new Color(1971474),
		new Color(6245189),
		new Color(7625812),
		new Color(8874850),
		new Color(4667443),
		new Color(4014144),
		new Color(4935503),
		new Color(5725276),
		new Color(3026992),
		new Color(5649214),
		new Color(6897227),
		new Color(8014168),
		new Color(4204078),
		new Color(3484480),
		new Color(4273487),
		new Color(4996700),
		new Color(2629680),
		new Color(3482392),
		new Color(4270878),
		new Color(4993571),
		new Color(2628114),
		new Color(3488029),
		new Color(4277796),
		new Color(5001770),
		new Color(2632470),
		new Color(6564384),
		new Color(8008487),
		new Color(9321518),
		new Color(4923160),
		new Color(1707787),
		new Color(2036237),
		new Color(2430480),
		new Color(1248008),
		new Color(8724770),
		new Color(10692906),
		new Color(12398641),
		new Color(6560025),
		new Color(6827076),
		new Color(8336979),
		new Color(9715553),
		new Color(5120307),
		new Color(4198676),
		new Color(5182745),
		new Color(6035741),
		new Color(3149071),
		new Color(1005662),
		new Color(1207411),
		new Color(1474182),
		new Color(737862),
		new Color(2647138),
		new Color(3308152),
		new Color(3837580),
		new Color(1985354),
		new Color(3940139),
		new Color(4859189),
		new Color(5647422),
		new Color(2955040),
		new Color(950109),
		new Color(1153906),
		new Color(1356933),
		new Color(679750),
		new Color(4605510),
		new Color(5658198),
		new Color(6579300),
		new Color(3421236),
		new Color(9993063),
		new Color(12228222),
		new Color(14200723),
		new Color(7494733),
		new Color(5862761),
		new Color(7180417),
		new Color(8365974),
		new Color(4413519)
	};
	

	public static final DyeColor TRANSPARENT = new DyeColor(BasicColor.VOID, 1);
}


class ColorManager{

	public static final Item DARKENING_ITEM = Items.COAL;
	public static final Item LIGHTENING_ITEM = Items.FEATHER;

	private static double getDistance(Color c1, Color c2) {
		double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		double r = c1.getRed() - c2.getRed();
		double g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2 + rmean / 256.0;
		double weightG = 4.0;
		double weightB = 2 + (255 - rmean) / 256.0;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}

	public static DyeColor closestColor(Color c){
		if(c.getAlpha() == 0){
			return DyeColor.TRANSPARENT;
		}

		DyeColor best = DyeColor.getPossibleColors()[0];
		double bestDistance = Double.POSITIVE_INFINITY;

		for(DyeColor candidate : DyeColor.getPossibleColors()){
			if(candidate.color.getAlpha() != 255){
				continue;
			}
			double distance = getDistance(candidate.color, c);
			if(distance < bestDistance){
				bestDistance = distance;
				best = candidate;
			}

		}

		return best;

	}


}


