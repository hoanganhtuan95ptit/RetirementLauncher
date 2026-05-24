git add .

git commit -m "fix: xử lý crash BackgroundService, crash TypeToken và tối ưu logic app list" \
-m "- Fix crash BackgroundServiceStartNotAllowedException bằng cách chuyển sang Foreground Service (API 26+).
- Fix crash IllegalStateException của Gson TypeToken khi bật R8/ProGuard.
- Refactor AppListViewModel sử dụng Set để quản lý app được chọn, đảm bảo tính đúng đắn khi tìm kiếm.
- Dọn dẹp code không sử dụng trong AppListFragment và ContactListFragment."

git push