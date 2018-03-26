#include "types.h"
#include <stdio.h>


int main() {
    char ch[2] = {'a','b'};
    u8 c = *ch;
    int len, stage_max, stage_cur;
#define FLIP_BIT(_ar, _b) do { \
    u8* _arf = (u8*)(_ar); \
    u32 _bf = (_b); \
    _arf[(_bf) >> 3] ^= (128 >> ((_bf) & 7)); \
} while (0)

    len = 1;
    stage_max = (len << 3) - 3;
    printf("max: %d\n", stage_max);
    printf("%c\n", c);
    for (stage_cur = 0; stage_cur < stage_max; stage_cur++) {
        FLIP_BIT(&c, stage_cur);
        FLIP_BIT(&c, stage_cur + 1);
        FLIP_BIT(&c, stage_cur + 2);
        FLIP_BIT(&c, stage_cur + 3);
        printf("c: %c\n", c);
        FLIP_BIT(&c, stage_cur);
        FLIP_BIT(&c, stage_cur + 1);
        FLIP_BIT(&c, stage_cur + 2);
        FLIP_BIT(&c, stage_cur + 3);
    }

return 1;
}
