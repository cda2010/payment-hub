(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('UniteOrganisation', UniteOrganisation)
        .factory('UniteOrganisationByType', UniteOrganisationByType)
        .factory('UniteOrganisationPaginer', UniteOrganisationPaginer)
        .factory('UniteOrganisationUsers', UniteOrganisationUsers);

    UniteOrganisation.$inject = ['$resource','BASE_URL_API'];
    UniteOrganisationByType.$inject = ['$resource','BASE_URL_API'];
    UniteOrganisationPaginer.$inject = ['$resource','BASE_URL_API'];
    UniteOrganisationUsers.$inject = ['$resource','BASE_URL_API'];

    function UniteOrganisation ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'unite-organisations/:id';

        return $resource(resourceUrl, {}, {
            'queryByTypeUoFSE': { method: 'GET',url:BASE_URL_API+'unite-organisations/FSE/typeUO', isArray: true},
            'queryByTypeUoMARCHAND': { method: 'GET',url:BASE_URL_API+'unite-organisations/MARCHAND/typeUO', isArray: true},
            'queryByType': { method: 'GET',url:BASE_URL_API+'unite-organisations/:typeId/typeUO', isArray: true},
            'query': { method: 'GET', isArray: true},
            'queryBycodeFSE': { method: 'GET',url:BASE_URL_API+'unite-organisations/codeFSE/:codeFSE', isArray: false},
            'queryByServiceProfil': {method: 'GET', url:BASE_URL_API+'findByProfilService', isArray: true},
            'sendInfoUoSMS': {method: 'GET', url:BASE_URL_API +'sendInfoOfUo' ,isArray: false},
            'queryByIdOptimise': { method: 'GET',url:BASE_URL_API+'unite-organisations-optimise/:id', isArray: false},
            'addCompteOrganisation': { method: 'GET', url:BASE_URL_API+'add-compte-organisation', isArray:false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    console.log('cool',data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
    function UniteOrganisationUsers ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'users/byUniteOrganisation/:uniteOrganisationId';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
        });
    }

    function UniteOrganisationByType ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'unite-organisationsByType/:typeId';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryOptimise': { method: 'GET',url:BASE_URL_API+'unite-organisationsByType-optimise/:typeId', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
        });
    }

    function UniteOrganisationPaginer ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'unite-organisations-paginer/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
        });
    }
})();
