#
# This file contains rules which are shared between multiple Makefiles.
#

#
# False targets.
#
.PHONY: dummy

#
# Special variables which should not be exported
#
unexport EXTRA_CFLAGS
unexport EXTRA_LDFLAGS
unexport EXTRA_ARFLAGS
unexport EXTRA_JFLAGS
unexport SUBDIRS
unexport SUB_DIRS
unexport ALL_SUB_DIRS
unexport O_TARGET
unexport L_TARGET
unexport J_TARGET
unexport O_OBJS
unexport L_OBJS
unexport J_OBJS

#
# Get things started.
#
first_rule: sub_dirs
	$(MAKE) all_targets

#
# Common rules
#

%.o: %.cpp
	$(CC) $(CFLAGS) $(EXTRA_CFLAGS) -c -o $@ $<

%.class: %.java
	$(JAVA) $(JFLAGS) $(EXTRA_JFLAGS) $<

#
#
#
all_targets: $(O_TARGET) $(L_TARGET) $(J_TARGET)

#
# A rule to make subdirectories
#
sub_dirs: dummy
ifdef SUB_DIRS
	set -e; for i in $(SUB_DIRS); do $(MAKE) -C $$i; done
endif

#
# A rule to do nothing
#
dummy:

#
# This is useful for testing
#
script:
	$(SCRIPT)

#
# Rule to compile a set of .o files into one .o file
#
ifdef O_TARGET
ALL_O = $(O_OBJS)
$(O_TARGET): $(ALL_O) 
	rm -f $@
ifneq "$(strip $(ALL_O))" ""
	$(LD) $(EXTRA_LDFLAGS) -r -o $@ $(ALL_O)
else
	$(AR) rcs $@
endif
endif

#
# Rule to compile a set of .o files into one .a file
#
ifdef L_TARGET
$(L_TARGET): $(L_OBJS)
	rm -f $@
	$(AR) $(EXTRA_ARFLAGS) rcs $@ $(L_OBJS)
endif

