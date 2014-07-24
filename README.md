SHA1PervasiveHashOffsetMap
==========================


HashMap that stores pairs of sha1, 64bit offset (i.e. byte[40] , byte[8]).

A bit slow in storing them (2-3 k per second)

Super fast in reading, using a 5million pairs database it can fetch 40k pairs per second.

All data is stored in disk.
