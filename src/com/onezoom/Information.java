package com.onezoom;

import android.graphics.Color;

public class Information {
	public static final String [] tabTitle = {
		"COLOUR KEY",
		"AUTHOR",
		"HOW TO USE"
	};
	
	public static final String guide = "Each leaf of the tree represents a species, the branches of the tree show how these species are connected to common ancestors over millions of years of evolution.  Use two fingers to pan and zoom around the tree, there is much more to it than meets the eye: zooming into your areas of interest will reveal over 22,000 species and their evolutionary connections.  Use the white circular signposts to help you navigate to the areas that interest you.  \n" + 
			"\n" + 
			"If you zoom into any leaf of the tree, you will see the common name, scientific name and conservation status of that species.  There are also buttons on the leaves, which are links to external resources.  If you press these buttons you will be redirected to further information about the relevant species, but note you need live Internet access to use this feature.  You can return to the tree view from a page of further information by pressing the tree button.  \n" + 
			"\n" + 
			"The colours of the tree correspond to IUCN Red List conservation statuses for each species – see the colour key tab for further details.  Interior nodes of the tree show dates of common ancestry between different groups, each of these nodes reports the number of descendent species, the date of the most recent common ancestor between them and (if applicable) the name that applies to that group of species.\n" + 
			"\n" + 
			"Pressing the back button will return you to the list of trees available for visualisation.\n" + 
			"\n" + 
			"Pressing the reload page button will reset your view of the tree of life back to the default with the complete tree filling the centre of the screen.\n" + 
			"\n" + 
			"Pressing the animation button will reveal further options for watching an animation of the tree growing with hundreds of millions of years of evolution being reduced to 1 minute.  You can use the play, pause, stop and reverse buttons to control the animation.  During the animation you can continue to explore the tree in the usual way by zooming and panning as before.  The back button returns you to the main menu, hiding all the animation control buttons.\n" + 
			"\n" + 
			"Pressing the search button will reveal further options for searching for a species of particular interest.  You can type in any scientific or common name to the search field using your on screen keyboard.  The left and right arrow buttons then enable you to move through the list of search hits in the tree.  The back button again returns you to the main menu, hiding the search control buttons.\n" + 
			"\n" + 
			"To explore OneZoom using the mouse on your desktop or laptop, please use the website www.onezoom.org.\n"
			+"\n";
	
	public static final int [] Colors = {
		Color.argb(255, 0, 180, 20),  //LC
		Color.argb(255, 65, 120, 0),  //NT
		Color.argb(255, 85, 85, 30),  //VU  
		Color.argb(255, 125, 50, 0),  //EN
		Color.argb(255, 210, 0, 10),  //CR
		Color.argb(255, 60, 50, 135), //EW
		Color.argb(255, 0, 0, 180),   //EX
		Color.argb(255, 80, 80, 80),  //DD
		Color.argb(255, 0, 0, 0)      //NE
		};
	
	public static final String [] meanning = {
		"Least Concern",
		"Near Threatened",
		"Vulnerable",
		"Endangered",
		"Critically Endangered",
		"Extinct in the Wild",
		"Extinct",
		"Data Deficient",
		"Not Evaluated"
	};
	
	public static final String authorAndCredit = "Kai Zhong developed this app from earlier OneZoom Software under the supervision of James Rosindell and Duncan Gillies.\n" + 
			"\n" + 
			"James Rosindell devised the original OneZoom concept, software and algorithms.\n" + 
			"\n" + 
			"We would like to thank Luke Harmon for his advice, Laura Nunes for her assistance with annotating the bird data and NERC for funding.\n" + 
			"\n" + 
			"This work is a contribution to Imperial College's Grand Challenges in Ecosystems and the Environment initiative.\n" + 
			"\n" + 
			"The data for this version of OneZoom came from the following sources:\n" + 
			"\n" + 
			"Mammal data: Bininda-Emonds OR, Cardillo M, Jones KE, MacPhee RD, Beck RM, et al. (2007) The delayed rise of present-day mammals. Nature 446: 507–512.\n" + 
			"\n" + 
			"Amphibian data: Isaac NJB, Redding DW, Meredith HM and Safi K (2012) Phylogenetically-Informed Priorities for Amphibian Conservation PLoS One\n" + 
			"\n" + 
			"Bird data: Jetz W, Thomas GH, Joy JB, Hartmann K, Mooers AO (2012) The global diversity of birds in space and time see also the special website birdtree.org\n" + 
			"\n" + 
			"Squamates data (excluding snakes): Bergmann PJ, Irschick1, DJ Vertebral Evolution and the Diversification of Squamate Reptiles Evolution 66(4) 2012\n" + 
			"\n" + 
			"Snake data: Pyron RA, Kandambi HKD, Hendry CR, Pushpamal V, Burbrink FT and Somaweera R. 2013. Genus-level molecular phylogeny of snakes reveals the origins of species richness in Sri Lanka. Molecular Phylogenetics and Evolution 66(2013): 969-978\n" + 
			"\n" + 
			"\n" + 
			"\n" + 
			"Turtle data: Jaffe AL, Slater GJ and Alfaro ME. The evolution of island gigantism and body size variation in tortoises and turtles. Biology Letters 2011 (7) doi: 10.1098/rsbl.2010.1084\n" + 
			"\n" + 
			"Crocodilian data: Oaks JR. A time-calibrated species tree of crocodylia reveals a recent radiation of the true crocodiles Evolution 2011 doi:10.1111/j.1558-5646.2011.01373.x\n" + 
			"\n" + 
			"Dates of common ancestry between larger clades: Timetree.org Hedges SB, Dudley J and Kumar S (2006). TimeTree: A public knowledge-base of divergence times among organisms. Bioinformatics 22: 2971-2972.\n" + 
			"\n" + 
			"Conservation status data: IUCN (2012) The IUCN Red List of Threatened Species. Version 2012.1. Available: http://www.iucnredlist.org. Downloaded on 25 May 2012\n" + 
			"\n" + 
			"Tree viewing software: Rosindell, J and Harmon, LJ (2012) OneZoom: A Fractal Explorer for the Tree of Life PLoS Biology.\n"
			+"\n";
}
