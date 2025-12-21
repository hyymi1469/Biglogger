import './App.css';
import MainPage from './MainPage';

import { BrowserRouter, Route, Routes } from 'react-router-dom'

const App = () => {
	return (
		<BrowserRouter>
		<Routes>
			<Route path="/" element={<MainPage />} exact/>
		</Routes>
		</BrowserRouter>

	);
}

export function ErrorFunc(param) {
	if(param === 10001)
	{
		alert("파일을 읽을 수 없습니다.");
	}
	else if (param === 10002) 
	{
		alert("마샬링이 잘못되었습니다.");
	}
	else if (param === 10003) 
	{
		alert("폴더 생성에 실패했습니다.");
	}
	else if (param === 10004) 
	{
		alert("같음 이름의 파일이 이미 존재합니다.");
	}
	else if (param === 10005) 
	{
		alert("에러가 발생했습니다. (공통 에러 코드)");
	}
	else if (param === 10006) 
	{
		alert("오픈할 파일이 없습니다.");
	}
	else if (param === 10007) 
	{
		alert("파일 저장 실패");
	}
	else if (param === 10008) 
	{
		alert("DB에 insert 방식이 잘못되었습니다.");
	}
	else if (param === 10009) 
	{
		alert("DB에 insert 하는 과정에서 문제가 발생했습니다. 에러로그 참고.");
	}
	else if (param === 10010) 
	{
		alert("정수형으로 바꿀 수 없는 문자입니다.");
	}
	else if (param === 10011) 
	{
		alert("해당 테이블을 조회하던 중 문제가 발생했습니다.");
	}
	else if (param === 10012) 
	{
		alert("해당 테이블을 조회를 순회하던 중 문제가 발생했습니다.");
	}
}

export default App;
