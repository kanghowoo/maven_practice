package com.mide.gangsaeng.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mide.gangsaeng.common.Cursor;
import com.mide.gangsaeng.common.CursorBasedRequest;
import com.mide.gangsaeng.common.CursorBasedResponse;

@Service
public class BoardServiceImpl implements BoardService {

    public static final int MAX_VALUE_OF_PAGE_SIZE = 30;
    private final BoardRepository boardRepository;

    @Autowired
    public BoardServiceImpl(BoardRepository boardRepository) {this.boardRepository =
            boardRepository;}

    public void write(BoardRequest request) {
        boardRepository.write(request);
    }

    @Override
    public BoardResponse read(long id) {
        Board board = boardRepository.read(id);
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .userId(board.getUserId())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    @Override
    public List<Board> getPage(int offset, int size) {
        size = Math.min(size, MAX_VALUE_OF_PAGE_SIZE);
        return boardRepository.getPage(offset, size);
    }

    @Override
    public CursorBasedResponse<Board> getPageByCursor(CursorBasedRequest request, int pageSize) {

        int size = pageSize + 1;
        Long prevCursor = null;
        Long nextCursor = null;

        if ("prev".equals(request.getDirection())) {
            List<Board> page = boardRepository.getPrevPage(request.getCursor(), size);

            page.sort((p1, p2) -> Long.compare(p2.getId(), p1.getId()));

            //이전 페이지에 새로운 데이터가 생길 경우
            if (page.size() < pageSize) {
                page = boardRepository.getNextPage(Long.MAX_VALUE, size); //root page;

                //다음 페이지 확인
                if (page.size() == size) {
                    page.remove(size - 1);
                    nextCursor = page.get(page.size() - 1).getId();
                }

                return new CursorBasedResponse<>(page, new Cursor(prevCursor, nextCursor));
            }

            if (page.size() == size) {
                page.remove(0);
                prevCursor = page.get(0).getId();
            }

            nextCursor = page.get(page.size() - 1).getId();

            return new CursorBasedResponse<>(page, new Cursor(prevCursor, nextCursor));
        }

        List<Board> page = boardRepository.getNextPage(request.getCursor(), size);

        if (page.size() == size) {
            page.remove(size - 1);
            nextCursor = page.get(page.size() - 1).getId();
        }


        if (!isRootCursor(request)) {
            prevCursor = page.get(0).getId();
        }

        return new CursorBasedResponse<>(page, new Cursor(prevCursor, nextCursor));
    }

    private boolean isRootCursor(CursorBasedRequest request) {
        return request.getCursor() == Long.MAX_VALUE && "next".equals(request.getDirection());
    }

}
