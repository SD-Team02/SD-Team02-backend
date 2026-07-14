# 배포 롤백 절차

`cd.yml`은 이미지를 `:latest`와 `:커밋해시` 두 태그로 Docker Hub에 올린다. `:latest`는 계속 덮어써지므로, 배포 후 문제가 생겼을 때 되돌리려면 "이전에 정상 동작했던 커밋 해시"로 직접 재배포해야 한다.

## 1. 되돌릴 커밋 해시 확인

- GitHub Actions 탭 → `CD` 워크플로우 실행 이력에서, 마지막으로 정상 배포(초록불)됐던 실행을 찾는다.
- 그 실행의 커밋 해시(`github.sha`, 짧은 형태로 커밋 목록에서도 확인 가능)를 복사한다.
- 또는 Docker Hub `sd-team02-backend` 저장소 → Tags 탭에서 태그 목록(커밋 해시들) 확인 가능.

## 2. EC2에서 수동 롤백

```bash
ssh -i <본인의 pem 키 경로> ubuntu@<EC2_HOST>

# 이전 정상 커밋 해시로 교체 (예시: abc1234)
docker pull <DOCKERHUB_USERNAME>/sd-team02-backend:abc1234

docker stop delivery-app
docker rm delivery-app

docker run -d \
  --name delivery-app \
  --restart unless-stopped \
  -p 127.0.0.1:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="..." \
  -e DB_USERNAME="..." \
  -e DB_PASSWORD="..." \
  -e JWT_SECRET="..." \
  -e AI_API_KEY="..." \
  -e MASTER_TOKEN="..." \
  -e MANAGER_TOKEN="..." \
  -e OWNER_TOKEN="..." \
  -e AWS_ACCESS_KEY_ID="..." \
  -e AWS_SECRET_ACCESS_KEY="..." \
  -e AWS_S3_BUCKET="..." \
  -e AWS_REGION="..." \
  <DOCKERHUB_USERNAME>/sd-team02-backend:abc1234
```

`<EC2_HOST>`, `<본인의 pem 키 경로>`, `<DOCKERHUB_USERNAME>`는 실제 값으로 바꿔서 실행한다 (이 문서에는 보안상 실제 IP/키 경로를 적지 않는다). 환경변수 값은 `cd.yml`에 들어있는 것과 동일해야 한다 (팀 내부적으로 안전한 곳에 백업해두거나, GitHub Secrets 값을 다시 확인해서 채운다).

## 3. 롤백 후 확인

```bash
curl -I https://meogjago.shop
docker logs --tail 100 delivery-app
```

정상 응답이 오는지, 로그에 에러가 없는지 확인한다.

## 참고

- `cd.yml`의 `deploy` job에 헬스체크가 추가되어 있어서, 배포 직후 앱이 응답하지 않으면 워크플로우 자체가 실패로 표시된다. 하지만 "배포는 성공했는데 나중에 문제가 생긴 경우"는 이 문서의 수동 절차로 대응해야 한다.
