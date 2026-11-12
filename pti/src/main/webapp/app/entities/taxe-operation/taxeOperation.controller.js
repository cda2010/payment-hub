(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeOperationController', TaxeOperationController);

    TaxeOperationController.$inject = ['InitCrud','$scope','TaxeOperation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TaxeOperationController(InitCrud,$scope, TaxeOperation, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title = "Liste des taxeOperations";
        vm.columns = [{
            name: 'taxeNom',
            title: 'taxe'
        },{
            name: 'modeCalcul',
            title: 'Mode calcul'
        },{
            name: 'appliqueSur',
            title: 'Applique sur'
        },{
            name:'libelleUniteOrganisation',
            title:'Organisation src'
        },{
            name:'libelleZone',
            title:'Zone destination'
        },{
            name: 'valeur',
            title: 'Valeur'
        }, {
            name: 'activated',
            title: 'Statut',
            type:'Boolean'
        }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                modeCalcul: null,
                appliqueSur: null,
                valeur: null,
                id: null
            },
            entity: TaxeOperation,
            addTemplateUrl: "taxe-operation/taxeOperation-dialog.html",
            deleteTemplateUrl: "taxe-operation/taxeOperation-delete-dialog.html",
            controller: "TaxeOperation",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
