(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypePieceController', TypePieceController);

    TypePieceController.$inject = ['InitCrud','$scope','$state', 'TypePiece', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TypePieceController(InitCrud,$scope,$state, TypePiece, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title="Liste des types de pieces";

        vm.columns = [{
            name: 'codeTypePiece',
            title: 'code'
        },{
            name: 'libelleTypePiece',
            title: 'libelle'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
            add:true
        };

        var datas={
            vm:vm,
            entityDatas:{
                codeTypePiece: null,
                libelleTypePiece: null,
                id: null
            },
            entity:TypePiece,
            addTemplateUrl:"type-piece/type-piece-dialog.html",
            deleteTemplateUrl:"type-piece/type-piece-delete-dialog.html",
            controller:"TypePiece",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();