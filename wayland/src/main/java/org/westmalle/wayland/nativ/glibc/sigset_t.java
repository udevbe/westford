package org.westmalle.wayland.nativ.glibc;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(@Field(name = "filler",
               cardinality = 128,
               type = CType.CHAR))
public final class sigset_t extends sigset_t_Jaccall_StructType {}
