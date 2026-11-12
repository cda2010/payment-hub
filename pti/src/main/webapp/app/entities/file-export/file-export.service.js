(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('FileExport', FileExport);

        FileExport.$inject = ['$resource', 'BASE_URL_API', 'DateUtils',];

    function FileExport ($resource, BASE_URL_API, DateUtils) {
        var resourceUrl =  'api/file-exports/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getTableValue': { method: 'GET', url:BASE_URL_API + '/file-export/getTableValue/:tableName/:colonne', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateGeneration = DateUtils.convertLocalDateToServer(copy.dateGeneration);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateGeneration = DateUtils.convertLocalDateToServer(copy.dateGeneration);
                    return angular.toJson(copy);
                }
            },

            'download':{
                method: 'GET',url:BASE_URL_API+"/download/:id", isArray: false ,responseType: 'arraybuffer' ,
                transformResponse: function (data, headers) {
                var   response = {};
                var fileName =headers("Content-Disposition").split('filename=')[1];
                if (data) {
                    response.data = data;
                    var blob = new Blob([response.data], { type: 'text/plain' });
                    var url = URL.createObjectURL(blob);
                    var a = document.createElement('a');
                    a.href = url;
                    //supprimer les double quotes qui entoure le nom du fichier
                    a.download = fileName.replace(/['"]/g, '');
                    a.click();
                    URL.revokeObjectURL(url);
                }
                return data;
            }}
        });
    }
})();