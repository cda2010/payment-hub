(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('FileExportParam', FileExportParam);

        FileExportParam.$inject = ['$resource', 'BASE_URL_API'];

    function FileExportParam ($resource, BASE_URL_API) {
        var resourceUrl =  'api/file-export-params/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByFileExport': { method: 'GET', url:BASE_URL_API + '/file-export-params/of-file-export/:id', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    return angular.toJson(copy);
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    // copy.dateGeneration = DateUtils.convertLocalDateToServer(copy.dateGeneration);
                    return angular.toJson(copy);
                }
            },
        });
    }
})();