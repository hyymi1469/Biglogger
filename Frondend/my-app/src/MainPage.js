import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { ErrorFunc } from './App.js';
import './MainPage.css';
import * as XLSX from 'xlsx';

const MainPage = () => {
  const [titleList, setTitleList] = useState([]);
  const [tlogData, setTlogData] = useState(null); // 서버에서 받은 전체 JSON 저장
  const [tlogTableList, setTlogTableList] = useState(null); // 서버에서 받은 전체 JSON 저장
  const [selectedTitle, setSelectedTitle] = useState(null);
  const fileInputRef = useRef(null);
  const [tableFilter, setTableFilter] = useState(''); // 추가: 테이블명 필터
  const [columnFilters, setColumnFilters] = useState({}); // 각 컬럼별 필터

  // useEffect를 함수로 분리
  const fetchMainPage = () => {
    const requestBody = ["hello", "world"];
    const config = {
      headers: {
        'Content-Type': 'application/json'
      }
    };
    let httpReq;
    if (process.env.NODE_ENV === 'development') {
      httpReq = axios.get('http://localhost:7001/api/main', requestBody, config);
    } else if (process.env.NODE_ENV === 'production') {
      httpReq = axios.get('/api/main', requestBody, config);
    }

    httpReq
      .then(response => {
        setTitleList(response.data.tables || []);
      })
      .catch(error => {
        alert(error.response.data);
        //ErrorFunc(error.request.status);
      });
  };

  useEffect(() => {
    fetchMainPage();
    setColumnFilters({});
  }, []);

  // 엑셀로 내보내기 함수
const handleExportExcel = () => {
  if (!tlogData) return;

  const fileName = prompt('엑셀 파일명을 입력하세요:', 'tlog_export.xlsx');
  if (!fileName) return;

  const workbook = XLSX.utils.book_new();
  let hasSheet = false;

  Object.entries(tlogData).forEach(([tableName, rows]) => {
    const parsedRows = rows.map(row =>
      typeof row === 'string' ? JSON.parse(row) : row
    );
    // columnFilters 적용
    const filteredRows = parsedRows.filter(row =>
      Object.entries(columnFilters).every(([header, filter]) =>
        !filter.trim() || String(row[header] || '').toLowerCase().includes(filter.toLowerCase())
      )
    );
    if (filteredRows.length === 0) return;
    const worksheet = XLSX.utils.json_to_sheet(filteredRows);
    XLSX.utils.book_append_sheet(workbook, worksheet, tableName);
    hasSheet = true;
  });

  if (!hasSheet) {
    alert('엑셀로 저장할 데이터가 없습니다.');
    return;
  }

  XLSX.writeFile(workbook, fileName.endsWith('.xlsx') ? fileName : `${fileName}.xlsx`);
};

  const handleTitleClick = async (title, index) => {
    let httpReq;
    const ReqTlogData = {
      TlogIndex: index,
      Title: title,
    };

    setColumnFilters({});
    setTableFilter(''); // 테이블명 필터 초기화

    if (title === selectedTitle)
    {
      // 이미 선택된 Title을 클릭하면 초기화
      //setTlogData({});
      setTlogTableList(null);
      setSelectedTitle(null);
      return;
    }

    if (process.env.NODE_ENV === 'development') {
      httpReq = axios.post('http://localhost:7001/api/tlogData', ReqTlogData, { timeout: 0 }); // 0은 무제한
    } else if (process.env.NODE_ENV === 'production') {
      httpReq = axios.post('/api/tlogData', ReqTlogData, { timeout: 0 });
    }

    httpReq
      .then(response => {
        // 서버에서 받은 전체 JSON을 tlogData에 저장
        setTlogTableList(response.data.TitleList);
        setSelectedTitle(response.data.Title); // 선택된 Title 저장
      })
      .catch(error => {
        ErrorFunc(error.request.status);
      });
  };

  const handleTableNameClick = async (title, table) => {

    setColumnFilters({});

    let httpReq;
    const ReqTlogTableData = {
      Title: title,
      TableName: table,
    };

    if (process.env.NODE_ENV === 'development') {
      httpReq = axios.post('http://localhost:7001/api/tlogTableData', ReqTlogTableData, { timeout: 0 }); // 0은 무제한
    } else if (process.env.NODE_ENV === 'production') {
      httpReq = axios.post('/api/tlogTableData', ReqTlogTableData, { timeout: 0 });
    }

    httpReq
      .then(response => {

        setTlogData({});
        if (!response.data || Object.keys(response.data).length === 0) {
          alert("데이터가 없습니다.");
          setTlogData({});
          return;
        }
        
        // 서버에서 받은 전체 JSON을 tlogData에 저장
        setTlogData(response.data || {});
      })
      .catch(error => {
        ErrorFunc(error.request.status);
      });
  };

  // 새로 추가: Title 삭제 핸들러
  const handleDeleteTitle = async (title) => {
    if (!window.confirm(`${title}을(를) 삭제하시겠습니까?`))
       return;
      
    try {
      let httpReq;
      const Req = { Title: title };
      if (process.env.NODE_ENV === 'development') {
        httpReq = axios.post('http://localhost:7001/api/deleteTitle', Req);
      } else {
        httpReq = axios.post('/api/deleteTitle', Req);
      }
      await httpReq;
      // 클라이언트에서 목록 갱신
      setTitleList(prev => prev.filter(it => it.Titile !== title));
      if (selectedTitle === title) {
        setSelectedTitle(null);
        setTlogTableList(null);
        setTlogData(null);
      }
    } catch (err) {
      ErrorFunc(err.request ? err.request.status : 0);
    }
  };

  const handleUploadClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
      fileInputRef.current.click();
    }
  };

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file)
       return;

    // 이미 업로드된 파일명(titleList에 존재)인지 확인
    if (titleList.some(item => item.Titile === file.name))
       {
      alert('같은 이름의 파일이 이미 존재합니다.');
      return;
    }

    if (!file.name.endsWith('.log')) {
      alert('.log 파일만 업로드할 수 있습니다.');
      return;
    }

    // 80MB(바이트) = 80 * 1024 * 1024
    const MAX_SIZE = 80 * 1024 * 1024;
    if (file.size > MAX_SIZE) {
      alert('업로드 실패. 파일 용량이 80MB를 넘기면 안됩니다.');
      return;
    }

    try {
      let uploadUrl;
      if (process.env.NODE_ENV === 'development') {
        uploadUrl = 'http://localhost:7001/api/upload';
      } else if (process.env.NODE_ENV === 'production') {
        uploadUrl = '/api/upload';
      }

      const formData = new FormData();
      formData.append('file', file); // 실제 파일

      await axios.post(uploadUrl, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      alert(`업로드 완료: ${file.name}`);
      fetchMainPage();

    } catch (err) {
      alert(err.request.responseText);
    }
  };

  return (
    <div className="main-container">
      <div className="header">
        <div className="header-title">
          BigLogger
        </div>
        <button
          className="upload-btn"
          onClick={handleUploadClick}
        >
          Upload
        </button>
        <input
          type="file"
          accept=".log"
          style={{ display: 'none' }}
          ref={fileInputRef}
          onChange={handleFileChange}
        />
      </div>
      <div className="content">
        <div className="left">
          {/* TitleInfoList 출력 */}
          <ul className="left-list" style={{ paddingLeft: 0, listStyle: 'none', marginLeft: 0 }}>
          {titleList.map((item, index) => (
            <li
              key={item.Index}
              style={{
                marginBottom: '24px',
                textAlign: 'left',
                paddingLeft: 0,
                marginLeft: 0,
                fontSize: '18px'
              }}
            >
              {index + 1}&nbsp;&nbsp;
              <span
                style={{ cursor: 'pointer', color: 'blue', textDecoration: 'underline' }}
                onClick={() => handleTitleClick(item.Titile, item.Index)}
              >
                <span>{item.Titile}</span>
                <button
                  onClick={e => { e.stopPropagation(); handleDeleteTitle(item.Titile); }}
                  title="삭제"
                  style={{
                    marginLeft: 25,
                    fontSize: 30,
                    background: 'transparent',
                    border: 'none',
                    color: '#d32f2f',
                    cursor: 'pointer'
                  }}
                >
                  ×
                </button>
                {/* tlogTableList가 있고, 현재 아이템이 선택된 경우 테이블 이름 리스트 출력 */}
                {tlogTableList && selectedTitle === item.Titile && (
                  <>
                    {/* 테이블명 필터 input 추가 */}
                    <input
                      type="text"
                      placeholder="테이블명 검색"
                      value={tableFilter}
                      onChange={e => setTableFilter(e.target.value)}
                      onClick={e => e.stopPropagation()}
                      style={{
                        margin: '8px 0 8px 16px',
                        padding: '4px 8px',
                        fontSize: '14px',
                        borderRadius: '4px',
                        border: '1px solid #ccc',
                        width: '80%',
                        boxSizing: 'border-box',
                        minWidth: '200px'
                      }}
                    />
                    <ul style={{ marginTop: 8, marginLeft: 16, fontSize: '14px', color: '#1976d2' }}>
                    {tlogTableList
                      .filter(table => table.toLowerCase().includes(tableFilter.toLowerCase()))
                      .map((table, idx) => {
                        // 하이라이트 처리
                        const filter = tableFilter.trim();
                        let displayTable = table;
                        if (filter) {
                          const regex = new RegExp(`(${filter})`, 'gi');
                          displayTable = table.replace(regex, match =>
                            `<span style="background:#ffe066">${match}</span>`
                          );
                        }
                        return (
                          <li
                            style={{ fontSize: '14px', color: '#1976d2', padding: '6px 0px' }}
                            key={idx}
                            onClick={e => {
                              e.stopPropagation();
                              handleTableNameClick(item.Titile, table);
                            }}
                            // dangerouslySetInnerHTML로 하이라이트 적용
                            dangerouslySetInnerHTML={{ __html: displayTable }}
                          />
                        );
                      })}
                    </ul>
                  </>
                )}
              </span>
            </li>
          ))}
        </ul>
        </div>
          <div className="right">
             
            {/* tlogData가 있고, 데이터가 1개 이상 있을 때만 엑셀 버튼 노출 */}
            {tlogData && Object.keys(tlogData).length > 0 && (
              <button
                className="export-btn"
                onClick={handleExportExcel}
                style={{
                  marginBottom: 16,
                  marginLeft: 0,
                  padding: '8px 16px',
                  fontSize: '16px',
                  backgroundColor: '#138c0f',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '4px',
                  textAlign: 'left', // 글씨만 왼쪽 정렬
                  cursor: 'pointer' // 마우스 오버 시 손가락 모양
                }}
              >
                엑셀로 저장
              </button>
            )}

            {/* tlogData가 있으면 모든 테이블 출력 */}
        {tlogData && Object.entries(tlogData).map(([tableName, rows]) => {
          const parsedRows = rows.map(row =>
            typeof row === 'string' ? JSON.parse(row) : row
          );
        
          // 모든 컬럼 필터 적용
          const filteredRows = parsedRows.filter(row =>
            Object.entries(columnFilters).every(([header, filter]) =>
              !filter.trim() || String(row[header] || '').toLowerCase().includes(filter.toLowerCase())
            )
          );
        
          const headers = filteredRows.length > 0 ? Object.keys(filteredRows[0]) : (parsedRows[0] ? Object.keys(parsedRows[0]) : []);
          return (
            <div key={tableName} style={{ marginBottom: 56 }}>
              <h4 style={{ margin: 0, color: '#1976d2', fontSize: 26 }}>{tableName}</h4>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ borderCollapse: 'collapse', width: '0%' }}>
                  <thead>
                    <tr>
                      {headers.map(header => (
                        <th
                          key={header}
                          style={{
                            border: '1px solid #ccc',
                            padding: '2px 5px',
                            backgroundColor: '#f0f0f0',
                            textAlign: 'center',
                            fontSize: 13,
                            whiteSpace: 'nowrap'
                          }}
                        >
                          {header}
                          <input
                            type="text"
                            placeholder={`${header} 검색`}
                            value={columnFilters[header] || ''}
                            onChange={e => setColumnFilters(filters => ({
                              ...filters,
                              [header]: e.target.value
                            }))}
                            style={{
                              marginLeft: 8,
                              padding: '2px 6px',
                              fontSize: '13px',
                              borderRadius: '4px',
                              border: '1px solid #ccc',
                              width: '120px'
                            }}
                          />
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRows.map((row, idx) => (
                      <tr key={idx}>
                        {headers.map(header => (
                          <td
                            key={header}
                            style={{
                              border: '1px solid #ddd',
                              padding: '6px 8px',
                              fontFamily: 'monospace',
                              fontSize: 13,
                              whiteSpace: 'nowrap'
                            }}
                          >
                            {/* 컬럼별 하이라이트: 검색어 부분만 */}
                            {columnFilters[header] && String(row[header]).toLowerCase().includes(columnFilters[header].toLowerCase()) ? (
                              <span
                                dangerouslySetInnerHTML={{
                                  __html: String(row[header]).replace(
                                    new RegExp(`(${columnFilters[header]})`, 'gi'),
                                    '<span style="background:#ffe066">$1</span>'
                                  )
                                }}
                              />
                            ) : (
                              row[header]
                            )}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          );
        })}
          </div>
      </div>
    </div>
  );
};

export default MainPage;