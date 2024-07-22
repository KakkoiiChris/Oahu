/*#################################################*
 #    ____  _       _    _ _    _                  #
 #   / __ \ \|/\   | |  | | |  | |      /\         #
 #  | |  | | /  \  | |__| | |  | | ____/  \_       #
 #  | |  | |/ /\ \ |  __  | |  | |\         |      #
 #  | |__| / ____ \| |  | | |__| | \         \/|   #
 #   \____/_/    \_\_|  |_|\____/   \___/\__   \   #
 #                                          \___\  #
 #        Copyright (C) 2019, KakkoiiChris         #
 *#################################################*/
package kakkoiichris.oahu.script;

import kakkoiichris.oahu.parser.Expr;

public sealed class Redirect extends RuntimeException {
    public static final class Break extends Redirect {
        private final Expr.Name label;
        
        public Break(Expr.Name label) {
            this.label = label;
        }
    
        public Expr.Name getLabel() {
            return label;
        }
    }
    
    public static final class Continue extends Redirect {
        private final Expr.Name label;
        
        public Continue(Expr.Name label) {
            this.label = label;
        }
        
        public Expr.Name getLabel() {
            return label;
        }
    }
    
    public static final class Exit extends Redirect {
        private final Object value;
        
        public Exit(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
    
    public static final class Return extends Redirect {
        private final Object value;
        
        public Return(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
    
    public static final class Throw extends Redirect {
        private final Object value;
        
        public Throw(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
}
