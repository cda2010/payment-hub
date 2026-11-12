(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('BackupData', BackupData);

        BackupData.$inject = ['$resource'];

    function BackupData ($resource) { 
        var resourceUrl =  'api/backup-data/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/backup-data' ,isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
