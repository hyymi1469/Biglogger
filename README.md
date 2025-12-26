# Biglogger
대용량 로그 적재 및 뷰어

# 사용기술
* Java(spring)
* React
* Mysql

# Overview
어느 서버든 통계를 내거나 유저들의 흐름을 보기 위해서는 로그가 필수적입니다.
서버마다 로그를 관리하는 법은 다르겠지만 로그는 항상 남기기 쉽고 언제든 추가로깅하기 쉽도록 구성해야 한다고 생각합니다.
하여 텍스트로 저장된 로그를 웹으로 편하게 볼 수 있는 백엔드, 프론트엔드를 만들었습니다.

- 실제 나의 개인 프로젝트에서 만든 게임이 있는데 거기에 활용되는 로그 뷰어
- 텍스트로만 이루어진 로그를 표로 만들어 보기 용이하게 하기 위해 개발
- 해당 표를 엑셀로 뽑아낼 수 있도록 하기
- 로그 한 번에 80MB로 제한
- 로그가 대용량이므로 DB에 저장할 때 bulk Insert 사용
- <img width="568" height="708" alt="이미지" src="https://github.com/user-attachments/assets/e8d4ff58-6889-43fe-aeac-b466de2e6e4f" />
첫 번째 컬럼 - 로그 테이블 타이틀 사용
두 번째 컬럼 - 텍스트로그가 많으므로 로그를 gzip으로 압축한 후 db의 BLOB으로 insert

- I/O Trhead -> LogicThread -> DBThread 구분하여 성능 끌어올리기
- 아래 사용법을 한 번에 요약(https://www.youtube.com/watch?v=he_Q0l8OGZY)

# 사용법
 - xml 포맷을 사용하여 로그를 제작 및 관리한다.
 - 다음과 같이 예를 들면 현재 접속자 로그를 일정 주기로 찍는 CurUserCountLogger로그의 xml이다.(Backend 폴더의 bigLogger.xml 에 있는 내용)
 - 해당 xml을 통해서 로그를 생성하는 함수를 제작함. 사용 언어에 따라 로그 함수를 자체적으로 생산하는 코드를 제작함.

<img width="1053" height="566" alt="image" src="https://github.com/user-attachments/assets/9b6977bb-5648-486a-8758-d2cc8ce30c59" />
<실제 라이브 중 텍스트로 남는 로그>

 - 구분자는 | 로 하여 필드마다 로그를 남김
 - 보통은 XML에 맞춰서 자동으로 로그 함수를 만들어주는 프로그램을 따로 돌림
 
<img width="1910" height="1020" alt="이미지" src="https://github.com/user-attachments/assets/43dcea44-556c-4e86-a84a-532d70b17320" />
 - 위처럼 18MB되는 로그 파일을 업로드 한다.(최대 80MB까지 올릴 수 있도록 설정)

<img width="507" height="152" alt="image" src="https://github.com/user-attachments/assets/8b8d59f2-31dd-420e-8e4a-7a99d87a1599" />
 
 - 18MB의 로그만 되어도 거의 8만줄에 달하는 로그가 있다.
 - 해당 로그를 한 줄씩 DB에 insert 하면 8만 번 insert 하고 시간도 오래 걸리므로 Batch Insert를 사용하여 대량의 insert를 빠르게 실행(테스트 결과 18MB insert 시 대략 2초 정도 걸림)

<img width="1441" height="754" alt="이미지 (1)" src="https://github.com/user-attachments/assets/0a862c94-aedd-4999-9bb9-c20a2c929711" />
 - 파일이 업로드 되면 좌측에 업로드 완성 된 log 이름이 뜸

 <img width="992" height="498" alt="이미지" src="https://github.com/user-attachments/assets/0c8b2e9e-165a-4bfa-a23f-83d979b3220d" />
 - 리액트에 검색 기능을 넣어서 로그 테이블을 검색하기 용이하게 함

 <img width="1853" height="774" alt="이미지 (1)" src="https://github.com/user-attachments/assets/d61f2df3-487c-450f-914d-59e26e76674a" />
 - 각 컬럼마다 검색을 할 수 있게 함( 각 컬럼마다 검색은 or 로 적용)
 - 적용된 내용을 엑셀로 뽑기 가능



