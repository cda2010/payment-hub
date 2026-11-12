(function() {
    'use strict';
    angular
        .module('ptiApp')
        .controller('TypeReclamationController', TypeReclamationController);

    TypeReclamationController.$inject = ['$scope','InitCrud','$state', 'TypeReclamation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];
    function TypeReclamationController($scope,InitCrud,$state, TypeReclamation, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title="Liste des Types de reclamation";

        vm.columns = [ {
            name: 'libelleTypeReclamation',
            title: 'Libelle'
            }
        ];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
        };

        var datas={
            vm:vm,
            entityDatas:{
                libelleTypeReclamation: null,
                id: null
            },
            entity:TypeReclamation,
            addTemplateUrl:"type-reclamation/type-reclamation-dialog.html",
            deleteTemplateUrl:"type-reclamation/type-reclamation-delete-dialog.html",
            controller:"TypeReclamation",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();