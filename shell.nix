{pkgs ? import <nixpkgs> {} }:
pkgs.mkShell{
	buildInputs = [
		pkgs.jdk25
		pkgs.gradle
		pkgs.libGL
		pkgs.glfw
		pkgs.flite
		pkgs.vulkan-loader
		pkgs.alsa-lib
	];
	shellHook = ''
	export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.lib.makeLibraryPath [
		pkgs.libGL
		pkgs.glfw
		pkgs.flite
		pkgs.vulkan-loader
		pkgs.alsa-lib
	]}"

	'';
}
