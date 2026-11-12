(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FournisseurServiceController', FournisseurServiceController);

    FournisseurServiceController.$inject = ['RequestUtils','InitCrud', '$scope', '$state', 'FournisseurService', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function FournisseurServiceController(RequestUtils,InitCrud, $scope, $state, FournisseurService, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title = "Liste des FSE";
        
        vm.columns = [{
            name: 'codeFse',
            title: 'code'
        }, {
            name: 'libelleFse',
            title: 'libelle'
        }];

        vm.actions = {
            view: false,
            edit: false,
            delete: false,
            add:false
        };

        var datas = {
            vm: vm,
            entityDatas: {
                codeFse: null,
                libelleFse: null,
                id: null
            },
            entity: RequestUtils.GetFournisseur,
            addTemplateUrl: "fournisseur-service/fournisseur-service-dialog.html",
            deleteTemplateUrl: "fournisseur-service/fournisseur-service-delete-dialog.html",
            controller: "FournisseurService",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();