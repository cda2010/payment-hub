(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('DetailReleveCompte', DetailReleveCompte);

    DetailReleveCompte.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function DetailReleveCompte ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'detail-releve-comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByReleve': { method: 'GET',url:BASE_URL_API+'detail-releve-comptes/by-releve', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.date = DateUtils.convertDateTimeFromServer(data.date);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
