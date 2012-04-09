TARGET=kingdomsgameplay
PKGPATH=fr/tobast/bukkit/$(TARGET)

all:
	@cd $(PKGPATH) && make
	jar cf $(TARGET).jar $(PKGPATH)/*.class plugin.yml licence.txt

clean:
	@cd $(PKGPATH) && make clean

