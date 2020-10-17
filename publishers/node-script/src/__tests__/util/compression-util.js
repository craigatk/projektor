const { ungzip } = require("node-gzip");

const extractUncompressedResultsPostData = async (mockAxios) => {
  expect(mockAxios.history.post.length).toBe(1);

  const postData = mockAxios.history.post[0].data;
  const uncompressedPostData = (await ungzip(postData)).toString();

  return uncompressedPostData;
};

module.exports = {
  extractUncompressedResultsPostData,
};
